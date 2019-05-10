package com.joker17.redundant.core;

import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.annotation.RdtMany;
import com.joker17.redundant.annotation.RdtOne;
import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import com.joker17.redundant.annotation.field.RdtFieldConditions;
import com.joker17.redundant.annotation.field.RdtFields;
import com.joker17.redundant.annotation.fill.RdtConditionTips;
import com.joker17.redundant.annotation.fill.RdtEntityTips;
import com.joker17.redundant.annotation.fill.RdtFieldRelyDetail;
import com.joker17.redundant.annotation.rely.*;
import com.joker17.redundant.model.*;
import com.joker17.redundant.model.commons.KeyTargetModel;
import com.joker17.redundant.model.commons.RdtRelyModel;
import com.joker17.redundant.model.commons.RdtRelyTargetColumnDetailModel;
import com.joker17.redundant.model.commons.RdtRelyTargetColumnModel;
import com.joker17.redundant.utils.ClassUtils;
import com.joker17.redundant.utils.PojoUtils;
import com.joker17.redundant.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class RdtPropertiesBuilder {

    protected final static Logger logger = LoggerFactory.getLogger(RdtPropertiesBuilder.class);

    private RdtResolver rdtResolver;
    private RdtProperties properties;

    public RdtPropertiesBuilder(RdtResolver rdtResolver, RdtProperties properties) {
        this.rdtResolver = rdtResolver;
        this.properties = properties;
    }


    public void builderClass(Class currentClass) {
        if (rdtResolver.isIgnoreClass(currentClass)) {
            if (currentClass != null) {
                logger.debug("ignore builder class : {}", currentClass.getName());
            }
            return;
        }

        ClassModel classModel = getClassModel(currentClass);

        if (classModel.getBuilderMark() != 0) {
            return;
        }
        classModel.setBuilderMark(1);

        Boolean isBaseClass = classModel.getBaseClass();

        List<Field> fieldList = classModel.getFieldList();
        List<Class<? extends Annotation>> sortAnnotationClassList = Arrays.asList(
                RdtRelys.class, RdtRely.class,
                RdtFieldConditionRelys.class, RdtFieldConditionRely.class, RdtFieldRely.class,
                RdtFieldConditions.class, RdtFields.class,
                RdtFieldCondition.class, RdtField.class,
                RdtOne.class, RdtMany.class);

        Map<Class, Map<Field, Annotation>> sortAnnotationClassMap = new LinkedHashMap<Class, Map<Field, Annotation>>(16);

        for (Field field : fieldList) {
            if (StringUtils.isEmpty(classModel.getPrimaryId())) {
                classModel.setPrimaryId(rdtResolver.getPrimaryId(currentClass, field));  //设置PrimaryId
            }

            //初始化列数据
            initColumn(classModel, field);

            List<Annotation> annotationList = rdtResolver.getAnnotations(field);
            if (!annotationList.isEmpty()) {
                //存放annotationClass对应的数据
                for (Class annotationClass : sortAnnotationClassList) {
                    Map<Field, Annotation> fieldAnnotationMap = sortAnnotationClassMap.get(annotationClass);
                    if (fieldAnnotationMap == null) {
                        fieldAnnotationMap = new LinkedHashMap<Field, Annotation>();
                        sortAnnotationClassMap.put(annotationClass, fieldAnnotationMap);
                    }

                    Iterator<Annotation> iterator = annotationList.iterator();
                    while (iterator.hasNext()) {
                        Annotation annotation = iterator.next();
                        if (annotationClass.equals(annotation.annotationType())) {
                            fieldAnnotationMap.put(field, annotation);
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        }

        if (StringUtils.isEmpty(classModel.getPrimaryId())) {

            String defaultIdKey = properties.getDefaultIdKey();
            if (classModel.getPropertyFieldMap().keySet().contains(defaultIdKey)) {
                classModel.setPrimaryId(defaultIdKey);
                logger.warn("rdt " + (isBaseClass ? " base " : "") + "class --- {} not found primary id, so use default primary id : {}, please make sure no problem.", classModel.getClassName(), defaultIdKey);
            }
        }

        if (StringUtils.isNotEmpty(classModel.getPrimaryId())) {
            //设置column is primaryId
            Column column = getColumn(classModel, classModel.getPrimaryId(), false);
            column.setIsPrimaryId(true);
        } else if (isBaseClass) {
            throw new IllegalArgumentException(classModel.getClassName() + " is base class, but has no primary id");
        }


        for (Class annotationClass : sortAnnotationClassMap.keySet()) {
            Map<Field, Annotation> fieldAnnotationMap = sortAnnotationClassMap.get(annotationClass);

            for (Field field : fieldAnnotationMap.keySet()) {
                Annotation annotation = fieldAnnotationMap.get(field);
                builderConfig(classModel, field, annotation);
            }
        }
        builderClassAfter(classModel);
    }

    private void builderClassAfter(ClassModel classModel) {

        Map<String, Map<Integer, RdtRelyModel>> propertyRelyDataMap = classModel.getPropertyRelyDataMap();
        if (!propertyRelyDataMap.isEmpty()) {
            builderModifyRelyDescribe(classModel, true);
            builderModifyRelyDescribe(classModel, false);
        }

        Map<Class, List<ModifyDescribe>> targetClassModifyDescribeMap = classModel.getTargetClassModifyDescribeMap();
        for (Class targetClass : targetClassModifyDescribeMap.keySet()) {
            List<ModifyDescribe> describeList = targetClassModifyDescribeMap.get(targetClass);
            for (ModifyDescribe describe : describeList) {
                rdtResolver.revealModifyDescribeLogs(describe, properties.getShowDescribe());
            }
        }
        Map<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>> targetClassModifyRelyDescribeMap = classModel.getTargetClassModifyRelyDescribeMap();

        for (Class targetClass : targetClassModifyRelyDescribeMap.keySet()) {
            Map<Column, Map<Integer, List<ModifyRelyDescribe>>> relyColumnDataMap = targetClassModifyRelyDescribeMap.get(targetClass);
            if (relyColumnDataMap == null) continue;

            for (Column relyColumn : relyColumnDataMap.keySet()) {
                Map<Integer, List<ModifyRelyDescribe>> groupDataMap = relyColumnDataMap.get(relyColumn);
                if (groupDataMap == null) continue;
                for (Integer group : groupDataMap.keySet()) {
                    List<ModifyRelyDescribe> describeList = groupDataMap.get(group);
                    for (ModifyRelyDescribe describe : describeList) {
                        rdtResolver.revealModifyRelyDescribeLogs(describe, properties.getShowDescribe());

                    }
                }
            }
        }
        classModel.setBuilderMark(2);
    }

    /**
     * 将依赖信息进行转换成对应的数据,若依赖列的group索引未被列引用不会出现该索引的配置
     * 如果依赖列没有被其他列所引用也不会产生ModifyRelyDescribe
     * @param classModel
     * @param condition
     */
    private void builderModifyRelyDescribe(ClassModel classModel, boolean condition) {
        Map<String, Map<Column, Map<String, RdtRelyTargetColumnModel>>> propertyTargetMap;

        if (condition) propertyTargetMap = classModel.getPropertyTargetConditionRelyMap();
        else propertyTargetMap = classModel.getPropertyTargetRelyMap();

        for (String property : propertyTargetMap.keySet()) {
            Column currentColumn = getColumn(classModel, property, false);//当前列信息

            Map<Column, Map<String, RdtRelyTargetColumnModel>> targetRelyMap = propertyTargetMap.get(property);
            if (targetRelyMap != null) {
                for(Column relyColumn : targetRelyMap.keySet()) {
                    Map<String, RdtRelyTargetColumnModel> relyTargetColumnModelMap = targetRelyMap.get(relyColumn);
                    if (relyTargetColumnModelMap != null) {
                        for (String relyTargetKey : relyTargetColumnModelMap.keySet()) {

                            List<Integer> keyResultList = rdtResolver.split(relyTargetKey, "-", Integer.class);
                            Integer index = keyResultList.get(1);

                            RdtRelyTargetColumnModel columnModel = relyTargetColumnModelMap.get(relyTargetKey);
                            Integer group = columnModel.getGroup(); //所属group
                            //class类型所对应的列
                            Map<Class, RdtRelyTargetColumnDetailModel> classTargetColumnDetailMap = columnModel.getClassTargetColumnDetailMap();

                            if (classTargetColumnDetailMap != null) {

                                for (Class targetClass : classTargetColumnDetailMap.keySet()) {
                                    ModifyRelyDescribe modifyRelyDescribe = getModifyRelyDescribe(classModel, getClassModel(targetClass), relyColumn, index, group);
                                    RdtRelyTargetColumnDetailModel detailModel = classTargetColumnDetailMap.get(targetClass);
                                    Column targetColumn = detailModel.getTargetColumn();
                                    if (condition) {
                                        ModifyCondition modifyCondition = getModifyCondition(currentColumn, targetColumn);
                                        modifyCondition.setNotAllowedNullTips(rdtResolver.getTipsContent(detailModel.getNotAllowedNullTips()));
                                        modifyRelyDescribe.getConditionList().add(modifyCondition);
                                    } else {
                                        ModifyColumn modifyColumn = null;
                                        try {
                                            modifyColumn = getModifyColumn(currentColumn, targetColumn);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        modifyColumn.setFillShowType(detailModel.getFillShowType());
                                        modifyColumn.setFillSaveType(detailModel.getFillSaveType());
                                        modifyColumn.setFillSaveIgnoresType(detailModel.getFillSaveIgnoresType());
                                        modifyColumn.setFillShowIgnoresType(detailModel.getFillShowIgnoresType());
                                        if (!modifyColumn.getColumn().getIsTransient()) {
                                            //只有为持久化字段时再修改默认的disableUpdate
                                            //modifyColumn.setDisableUpdate(detailModel.getDisableUpdate());
                                        }
                                        modifyRelyDescribe.getColumnList().add(modifyColumn);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void builderConfig(ClassModel classModel, Field field, Annotation annotation) {
        Column column = getColumn(classModel, field);
        if (annotation != null) {
            if (annotation instanceof RdtFields) {
                RdtFields rdtAnnotations = (RdtFields) annotation;
                for (RdtField rdtAnnotation : rdtAnnotations.value()) {
                    builderRdtFieldConfigData(classModel, column, rdtAnnotation);
                }
            } else if (annotation instanceof RdtField) {
                builderRdtFieldConfigData(classModel, column, (RdtField) annotation);
            } else if (annotation instanceof RdtFieldConditions) {
                RdtFieldConditions rdtAnnotations = (RdtFieldConditions) annotation;
                for (RdtFieldCondition rdtAnnotation : rdtAnnotations.value()) {
                    builderRdtFieldConditionConfigData(classModel, column, rdtAnnotation);
                }
            } else if (annotation instanceof RdtFieldCondition) {
                builderRdtFieldConditionConfigData(classModel, column, (RdtFieldCondition) annotation);
            } else if (annotation instanceof RdtOne) {
                builderComplexModelConfigData(classModel, column, true);
            } else if (annotation instanceof RdtMany) {
                builderComplexModelConfigData(classModel, column, false);
            } else if (annotation instanceof RdtRely) {
                RdtRely rdtAnnotation = (RdtRely) annotation;
                builderRdtRelyConfigData(classModel, column, rdtAnnotation);
            } else if (annotation instanceof RdtRelys) {
                RdtRelys rdtAnnotations = (RdtRelys) annotation;
                for (RdtRely rdtAnnotation : rdtAnnotations.value()) {
                    builderRdtRelyConfigData(classModel, column, rdtAnnotation);
                }
            } else if (annotation instanceof RdtFieldConditionRelys) {
                RdtFieldConditionRelys rdtAnnotations = (RdtFieldConditionRelys) annotation;
                for (RdtFieldConditionRely rdtAnnotation : rdtAnnotations.value()) {
                    builderPropertyRelyGroupConfigData(classModel, column, rdtAnnotation);
                }
            }  else if (annotation instanceof RdtFieldConditionRely) {
                builderPropertyRelyGroupConfigData(classModel, column, annotation);
            } else if (annotation instanceof RdtFieldRely) {
                builderPropertyRelyGroupConfigData(classModel, column, annotation);
            }
        }
    }

    private void builderRdtFieldConfigData(ClassModel classModel, Column column, RdtField rdtAnnotation) {
        Class targetClass = rdtAnnotation.target();  //当前属性要修改所对应的class
        loadClassWithAnnotation(targetClass);

        String targetProperty = rdtAnnotation.property();  //要修改属性的对应的别名
        targetProperty = StringUtils.isEmpty(targetProperty) ? column.getProperty() : targetProperty;
        int index = rdtAnnotation.index();

        //获取target class对应的列
        ClassModel targetClassModel = getClassModel(targetClass);
        Column targetColumn = getColumn(targetClassModel, targetProperty);

        columnCompareVerification(column, targetColumn, classModel, targetClassModel, false);  //验证条件的属性类型一致

        //添加修改属性内容
        ModifyDescribe modifyDescribe = getModifyDescribe(classModel, targetClassModel, index);
        ModifyColumn modifyColumn = getModifyColumn(column, targetColumn);
        modifyColumn.setFillSaveType(rdtAnnotation.fillSave());
        modifyColumn.setFillShowType(rdtAnnotation.fillShow());
        modifyDescribe.getColumnList().add(modifyColumn);
    }

    private void builderRdtFieldConditionConfigData(ClassModel classModel, Column column, RdtFieldCondition rdtAnnotation) {
        Class targetClass = rdtAnnotation.target();  //对应的class
        loadClassWithAnnotation(targetClass);

        String targetProperty = rdtAnnotation.property();  //对应的别名
        targetProperty = StringUtils.isEmpty(targetProperty) ? column.getProperty() : targetProperty;
        int index = rdtAnnotation.index();

        //获取target class对应的列
        ClassModel targetClassModel = getClassModel(targetClass);
        Column targetColumn = getColumn(targetClassModel, targetProperty);
        columnCompareVerification(column, targetColumn, classModel, targetClassModel, true);  //验证属性类型一致

        //添加修改条件
        ModifyDescribe modifyDescribe = getModifyDescribe(classModel, targetClassModel, index);
        ModifyCondition modifyCondition = getModifyCondition(column, targetColumn);
        modifyCondition.setNotAllowedNullTips(rdtResolver.getTipsContent(rdtAnnotation.nullTips()));
        modifyDescribe.getConditionList().add(modifyCondition);
    }


    /**
     * 解析@RdtFieldConditionRely和@RdtFieldRely数据为对应字段不同group index下所依赖字段类型值对应class所使用的属性列
     * @param classModel
     * @param column
     * @param annotation
     */
    private void builderPropertyRelyGroupConfigData(ClassModel classModel, Column column, Annotation annotation) {
        boolean isConditionRely = false;
        int group;
        int relyIndex;
        String[] targetPropertys;
        Class actualClass;

        String relyProperty;  //依赖的字段别名

        List<RdtFillType> fillShowTypeList = new ArrayList<RdtFillType>();
        List<RdtFillType> fillSaveTypeList = new ArrayList<RdtFillType>();
        List<String> nullTipsList = new ArrayList<String>();

        Map<Class, RdtRelyTargetColumnDetailModel> classTargetColumnDetailMap = new HashMap<Class, RdtRelyTargetColumnDetailModel>();

        RdtFieldRelyDetail[] fieldRelyDetails = new RdtFieldRelyDetail[0];
        if (annotation instanceof RdtFieldConditionRely) {
            isConditionRely = true;
            RdtFieldConditionRely rdt = (RdtFieldConditionRely) annotation;
            group = rdt.group();
            relyIndex = rdt.index();
            relyProperty = rdt.property();

            targetPropertys = rdt.targetPropertys();
            actualClass = rdt.target() == Void.class ? null : rdt.target();
            nullTipsList.addAll(rdtResolver.parseAnnotationValues(rdt.nullTips(), String.class));
        } else if (annotation instanceof RdtFieldRely) {
            RdtFieldRely rdt = (RdtFieldRely) annotation;
            group = rdt.group();
            relyIndex = rdt.index();
            relyProperty = rdt.property();
            targetPropertys = rdt.targetPropertys();
            actualClass = rdt.target() == Void.class ? null : rdt.target();
            for (RdtFillType type : rdt.fillShow()) {
                fillShowTypeList.add(type);
            }

            for (RdtFillType type : rdt.fillSave()) {
                fillSaveTypeList.add(type);
            }

            fieldRelyDetails = rdt.details();

        } else throw new IllegalArgumentException("rdt builder rely config not support this type");

        String property = column.getProperty();
        Column relyColumn = getColumn(classModel, relyProperty);

        String hintPrefix = classModel.getClassName() + " property " + property + " field ";
        if (isConditionRely) hintPrefix += "condition ";
        hintPrefix += "rely config " + rdtResolver.getConditionMark(Arrays.asList("column", "group", "index"), Arrays.asList(new Object[] {relyProperty, group, relyIndex})) + " has error, caused by :";

        //获取rely column第group组所对应的rdtRelyModel
        Map<Integer, RdtRelyModel> relyDataMap = classModel.getPropertyRelyDataMap().get(relyColumn.getProperty());
        RdtRelyModel rdtRelyModel = null;
        if (relyDataMap != null) rdtRelyModel = relyDataMap.get(group);
        //需要有对应的rely model数据
        if (rdtRelyModel == null) throw new IllegalArgumentException(hintPrefix +  " corresponding rely data config named " +  relyColumn.getProperty() + " not exist");

        String relyTargetKey = group + "-" + relyIndex;

        //初始化当前column所对应rely column关于第group-index组的数据
        Map<String, Map<Column, Map<String, RdtRelyTargetColumnModel>>> propertyTargetConditionRelyMap;
        if (isConditionRely) {
            propertyTargetConditionRelyMap = classModel.getPropertyTargetConditionRelyMap();
        } else {
            propertyTargetConditionRelyMap = classModel.getPropertyTargetRelyMap();
        }
        Map<Column, Map<String, RdtRelyTargetColumnModel>> relyColumnGroupModelMap = propertyTargetConditionRelyMap.get(property);
        if (relyColumnGroupModelMap == null) {
            relyColumnGroupModelMap = new LinkedHashMap<Column, Map<String, RdtRelyTargetColumnModel>>();
            propertyTargetConditionRelyMap.put(property, relyColumnGroupModelMap);
        }

        Map<String, RdtRelyTargetColumnModel> relyTargetColumnModelMap = relyColumnGroupModelMap.get(relyColumn);
        if (relyTargetColumnModelMap == null) {
            relyTargetColumnModelMap = new HashMap<String, RdtRelyTargetColumnModel>();
            relyColumnGroupModelMap.put(relyColumn, relyTargetColumnModelMap);
        }


        //初始化数据
        RdtRelyTargetColumnModel rdtRelyTargetColumnModel = relyTargetColumnModelMap.get(relyTargetKey);
        if (rdtRelyTargetColumnModel != null) {
            throw new IllegalArgumentException(hintPrefix + " already exist");
        }

        Map<Class, KeyTargetModel> targetClassValueMap = rdtRelyModel.getTargetClassValueMap();

        List<Class> keyTargetClassList = new ArrayList<Class>(targetClassValueMap.keySet());
        boolean hasActualClass = actualClass != null;
        if (hasActualClass) {
            //check actual class in key class list
            if (!keyTargetClassList.contains(actualClass)) {
                throw new IllegalArgumentException(hintPrefix + " target class " + actualClass.getName() + " not in rely column class list");
            }
        }

        rdtRelyTargetColumnModel = new RdtRelyTargetColumnModel();
        relyTargetColumnModelMap.put(relyTargetKey, rdtRelyTargetColumnModel);
        rdtRelyTargetColumnModel.setGroup(group);


        if (fieldRelyDetails != null) {
            for (RdtFieldRelyDetail detail : fieldRelyDetails) {
                Class targetClass = detail.target();
                if (!keyTargetClassList.contains(targetClass)) {
                    throw new IllegalArgumentException(hintPrefix + " @RdtFieldRelyDetail target class " + targetClass.getName() + " not in rely column class list");
                }
                if (classTargetColumnDetailMap.get(detail.target()) != null) {
                    throw new IllegalArgumentException(hintPrefix + " @RdtFieldRelyDetail target class " + targetClass.getName() + " must be only one");
                }
                RdtRelyTargetColumnDetailModel detailModel = new RdtRelyTargetColumnDetailModel();

                KeyTargetModel keyTargetModel = targetClassValueMap.get(targetClass);
                List<Object> targetValueList = keyTargetModel.getValueList();

                List<Object> fillSaveIgnoresType = rdtResolver.parseAnnotationValues(detail.fillSaveIgnoresType(), rdtRelyModel.getValType());
                List<Object> fillShowIgnoresType = rdtResolver.parseAnnotationValues(detail.fillShowIgnoresType(), rdtRelyModel.getValType());
                if (!fillSaveIgnoresType.isEmpty()) {
                    //check value
                    for (Object value : fillSaveIgnoresType) {
                        if (!targetValueList.contains(value)) {
                            //忽略更新的值必须在值列表中
                            throw new IllegalArgumentException(hintPrefix + " @RdtFieldRelyDetail target class " + targetClass.getName() + " fill save val " + value + " must be in value: " + targetValueList);
                        }
                    }
                }
                if (!fillShowIgnoresType.isEmpty()) {
                    //check value
                    for (Object value : fillShowIgnoresType) {
                        if (!targetValueList.contains(value)) {
                            //忽略更新的值必须在值列表中
                            throw new IllegalArgumentException(hintPrefix + " @RdtFieldRelyDetail target class " + targetClass.getName() + " fill show val " + value + " must be in value: " + targetValueList);
                        }
                    }
                }
                detailModel.setFillSaveIgnoresType(fillSaveIgnoresType);
                detailModel.setFillShowIgnoresType(fillShowIgnoresType);
                classTargetColumnDetailMap.put(targetClass, detailModel);
            }
        }


        //初始化各个class所使用的column对象
        Map<Class, Column> classTargetColumnMap = new HashMap<Class, Column>();
        int targetEqPropertysLength = targetPropertys.length;
        int classSize = keyTargetClassList.size();

        List<String> usePropertyList = new ArrayList<String>();
        if (targetEqPropertysLength == 0) {
            //默认使用当前属性名称
            usePropertyList.add(property);
        } else {
            usePropertyList.addAll(rdtResolver.parseAnnotationValues(targetPropertys, String.class));
        }

        if (usePropertyList.size() > 1 && classSize != usePropertyList.size()) {
            throw new IllegalArgumentException(hintPrefix + " should setting propertys according the corresponding rely data config target class order and expect size is " + classSize);
        }

        String alias = null;
        boolean oneProperty = usePropertyList.size() == 1;
        if (oneProperty) alias = usePropertyList.get(0);

        if (hasActualClass && !oneProperty) {
            throw new IllegalArgumentException(hintPrefix + " has appoint target class, so property must be only one");
        }

        int expectShowSize = -1;
        int expectSaveSize = -1;
        int nullTipsSize = nullTipsList.size();
        if (!isConditionRely) {
            expectShowSize = 1;
            expectSaveSize = 1;
            int fillShowTypeSize = fillShowTypeList.size();
            int fillSaveTypeSize = fillSaveTypeList.size();
            if (!hasActualClass) {
                //若非1时必须全部配置
                expectShowSize = fillShowTypeSize != 1 ? classSize : expectShowSize;
                expectSaveSize = fillSaveTypeSize != 1 ? classSize : expectSaveSize;
            }

            if (fillShowTypeSize != expectShowSize) {
                throw new IllegalArgumentException(hintPrefix + " expect fill fillShowIgnoresType type length is " + expectShowSize);
            }

            if (fillSaveTypeSize != expectSaveSize) {
                throw new IllegalArgumentException(hintPrefix + " expect fill save type length is " + expectSaveSize);
            }
        } else {
            if (nullTipsSize > 1 && nullTipsSize != classSize) {
                throw new IllegalArgumentException(hintPrefix + " expect fill save type length is " + expectSaveSize);
            }
        }

        for (int i = 0; i < classSize; i ++) {
            Class targetClass = keyTargetClassList.get(i);
            if (hasActualClass) {
                targetClass = actualClass;
            }
            RdtRelyTargetColumnDetailModel detailModel = classTargetColumnDetailMap.get(targetClass);

            boolean hasDetailModel = detailModel != null;
            if (!hasDetailModel) {
                detailModel = new RdtRelyTargetColumnDetailModel();
                classTargetColumnDetailMap.put(targetClass, detailModel);
            }

            if (!isConditionRely) {
                //设置rdt fill type
                RdtFillType fillShowType;
                RdtFillType fillSaveType;

                if (expectShowSize == 1) {
                    fillShowType = fillShowTypeList.get(0);
                } else {
                    fillShowType = fillShowTypeList.get(i);
                }

                if (expectSaveSize == 1) {
                    fillSaveType = fillSaveTypeList.get(0);
                } else {
                    fillSaveType = fillSaveTypeList.get(i);
                }
                detailModel.setFillSaveType(fillSaveType);
                detailModel.setFillShowType(fillShowType);

                if (!hasDetailModel) {
                    //没有额外配置时则使用全局
                    //detailModel.setDisableUpdate(disableUpdate);
                }

            } else {
                if (nullTipsSize > 0) {
                    String nullTips = nullTipsSize == 1 ? nullTipsList.get(0) : nullTipsList.get(i);
                    detailModel.setNotAllowedNullTips(nullTips);
                }

            }


            if (!oneProperty) {
                alias = usePropertyList.get(i);
            }


            builderPropertyRelyGroupTypeData(classModel, column, isConditionRely, targetClass, alias, classTargetColumnMap, hintPrefix, " target type appointed ");

            detailModel.setTargetColumn(classTargetColumnMap.get(targetClass));
            classTargetColumnDetailMap.put(targetClass, detailModel);

            if (hasActualClass) {
                break;
            }
        }
        rdtRelyTargetColumnModel.setClassTargetColumnDetailMap(classTargetColumnDetailMap);
    }

    private void builderPropertyRelyGroupTypeData(ClassModel classModel, Column column, boolean isConditionRely, Class type, String typePropertyAlias, Map<Class, Column> classTargetColumnMap, String hintPrefix, String describe) {
        if (describe == null) describe = "";
        hintPrefix += describe;
        if (type == null) {
            if (typePropertyAlias != null)
                throw new IllegalArgumentException(hintPrefix + " not exist and property is not allowed");
        } else {
            Column currentColumn = classTargetColumnMap.get(type);
            if (currentColumn == null && typePropertyAlias == null) {
                throw new IllegalArgumentException(hintPrefix + type.getName() + " property not found, please to appoint.");
            } else {
                if (currentColumn == null) {
                    ClassModel typeClassModel = getClassModel(type);
                    try {
                        currentColumn = getColumn(typeClassModel, typePropertyAlias);
                        columnCompareVerification(column, currentColumn, classModel, typeClassModel, isConditionRely);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(hintPrefix + " " + e.getMessage());
                    }
                    if (!typeClassModel.getBaseClass()) {
                        throw new IllegalArgumentException(hintPrefix + " " + typeClassModel.getClassName() + " used for field target class, so must be base class");
                    }

                    judgeClassRelation(classModel, typeClassModel);
                    classTargetColumnMap.put(type, currentColumn);//设置属性对应的class所使用的列信息
                } else {  //需要一致
                    if (typePropertyAlias != null && !currentColumn.getAlias().equals(typePropertyAlias)) {
                        throw new IllegalArgumentException(hintPrefix + " " + type.getName() + " property must identical --- " + currentColumn.getAlias());
                    }
                }
            }
        }
    }

    private Class getRdtValType(Class configType, Class propertyClass, String notAllowedEnumErrorPrefix) {
        Class valType = configType == Void.class ? propertyClass : configType;
        boolean propertyClassIsEnum = propertyClass.isEnum();
        if (propertyClassIsEnum) {
            if (valType != int.class && valType != String.class && valType != propertyClass) {
                throw new IllegalArgumentException(notAllowedEnumErrorPrefix +
                        " type belong to enum, should by default or designate use ordinal type - int.class or name type - String.class");
            }
        }
        return valType;
    }
    /**
     * 解析@RdtRely数据
     * @param classModel
     * @param column
     * @param rdtRely
     */
    private void builderRdtRelyConfigData(ClassModel classModel, Column column, RdtRely rdtRely) {
        int group = rdtRely.group();
        boolean isUniqueValue = rdtRely.unique();
        String property = column.getProperty();
        String hintPrefix = classModel.getClassName() + " property " + property + " rely data config :" + " group index " + group + " has error, caused by :";

        //初始化当前column property的第group组所对应的rdtRelyModel
        Map<String, Map<Integer, RdtRelyModel>> propertyRelyDataMap = classModel.getPropertyRelyDataMap();
        Map<Integer, RdtRelyModel> currentRelyDataMap = propertyRelyDataMap.get(property);
        if (currentRelyDataMap == null) {
            currentRelyDataMap = new HashMap<Integer, RdtRelyModel>();
            propertyRelyDataMap.put(property, currentRelyDataMap);
        }
        RdtRelyModel rdtRelyModel = currentRelyDataMap.get(group);
        if (rdtRelyModel != null) {
            throw new IllegalArgumentException(hintPrefix + " already exist");
        }
        rdtRelyModel = new RdtRelyModel();
        currentRelyDataMap.put(group, rdtRelyModel);

        rdtRelyModel.setNotAllowedTypeTips(rdtResolver.getTipsContent(rdtRely.typeTips()));

        //获取@RdtRely的值类型
        Class propertyClass = column.getPropertyClass();
        Class valType = getRdtValType(rdtRely.valType(), propertyClass, hintPrefix);
        if (ClassUtils.familyClass(valType, Collection.class)) {
            throw  new IllegalArgumentException(hintPrefix +
                    " type not support collection type");
        }

        Class unknownType = rdtRely.unknownType() == Void.class ? null : rdtRely.unknownType();
        List<Class> emptyTargetValueList = new ArrayList<Class>();
        //处理 target class所对应的值
        Map<Class, KeyTargetModel> targetClassValueMap = rdtRelyModel.getTargetClassValueMap();
        Map<Object, Class> typeValClassMap = new HashMap<Object, Class>(16);
        for (KeyTarget keyTarget : rdtRely.value()) {

            //验证并添加target class列表
            Class target = keyTarget.target();
            KeyTargetModel keyTargetModel = targetClassValueMap.get(target);
            if (keyTargetModel != null) {
                //target class只允许出现一次,通过value配置相关类型值
                throw new IllegalArgumentException(hintPrefix +
                        " @KeyTarget target type " + target.getName() + " only allowed setting once");
            }
            keyTargetModel = new KeyTargetModel();
            targetClassValueMap.put(target, keyTargetModel);

            //解析处理当前target class的val值
            List<Object> targetValueList = rdtResolver.parseAnnotationValues(keyTarget.value(), valType, hintPrefix + " @KeyTarget target type " + target.getName() + " has no enum " + valType.getName() + " type val: ");

            for (Object value : targetValueList) {
                if (isUniqueValue) {
                    //验证当处于unique时值是否已存在
                    Class typeClass = typeValClassMap.get(value);
                    if (typeClass != null) {
                        throw new IllegalArgumentException(hintPrefix + " @KeyTarget target type " + target.getName() + " type val "+ value + " has already exist the before target type " + typeClass.getName());
                    }
                    //将此类型值与class绑定
                    typeValClassMap.put(value, target);
                }
            }

            if (targetValueList.isEmpty()) {
                emptyTargetValueList.add(target);
            }


            String[] ignoreUpdateValueArray = keyTarget.ignoreUpdateValue();
            List<Object> updateIgnoresValueList;

            if (ignoreUpdateValueArray != null && ignoreUpdateValueArray.length == 1 && StringUtils.equals("$value", ignoreUpdateValueArray[0])) {
                updateIgnoresValueList = new ArrayList<Object>(targetValueList);
            } else {
                updateIgnoresValueList = rdtResolver.parseAnnotationValues(ignoreUpdateValueArray, valType, hintPrefix + " @KeyTarget target type " + target.getName() + " has no enum " + valType.getName() + " type val: ");
                if (!updateIgnoresValueList.isEmpty()) {
                    for (Object value : updateIgnoresValueList) {
                        if (!targetValueList.contains(value)) {
                            //忽略更新的值必须在值列表中
                            throw new IllegalArgumentException(hintPrefix + " @KeyTarget target type " + target.getName() + " update ignores val " + value + " must be in value: " + targetValueList);
                        }
                    }

                    if (updateIgnoresValueList.size() > targetValueList.size()) {
                        throw new IllegalArgumentException(hintPrefix + " @KeyTarget target type " + target.getName() + " update ignores val: " + updateIgnoresValueList + String.format(" size(%s) ", updateIgnoresValueList.size()) + "must be lt value: " + targetValueList + String.format(" size(%s)", targetValueList.size()));
                    }
                }
            }


            keyTargetModel.setValueList(targetValueList);
            String typeNotAllowedTips = rdtResolver.getTipsContent(keyTarget.typeNotAllowedTips());
            keyTargetModel.setNotAllowedTypeTips(StringUtils.isNotEmpty(typeNotAllowedTips) ? typeNotAllowedTips : rdtRelyModel.getNotAllowedTypeTips());
            keyTargetModel.setUpdateIgnoresValueList(updateIgnoresValueList);
        }


        //处理unknown type
        if (unknownType != null) {
            KeyTargetModel unknownTypeKeyTargetModel = targetClassValueMap.get(unknownType);
            if (unknownTypeKeyTargetModel == null) {
                throw new IllegalArgumentException(hintPrefix +
                        " @KeyTarget not config target type with unknown type " + unknownType.getName());
            }
            if (!emptyTargetValueList.isEmpty()) {
                emptyTargetValueList.remove(unknownType);
            }
            List<Object> unknownTypeValList = unknownTypeKeyTargetModel.getValueList();
            List<Object> unknownNotExistValues = rdtRelyModel.getUnknownNotExistValues();
            //先存放已存在的全部类型值
            for (KeyTargetModel keyTargetModel : targetClassValueMap.values()) {
                unknownNotExistValues.addAll(keyTargetModel.getValueList());
            }

            //获取为unknownType时所对应的全部值
            if (!unknownTypeValList.isEmpty()) {
                //只保留除unknownType外所存在的类型值
                unknownNotExistValues.removeAll(unknownTypeValList);
            }
        }

        for (Class target : emptyTargetValueList) {
            //target class必须存在类型值限定
            throw new IllegalArgumentException(hintPrefix +
                    " @KeyTarget about target type " + target.getName() + " should config value list or designated as unknownType.");
        }


        List<Object> allowValueList = rdtResolver.parseAnnotationValues(rdtRely.allowValues(), valType, hintPrefix + " has no enum " + valType.getName() + " type val: " );
        rdtRelyModel.getAllowValues().addAll(allowValueList);

        rdtRelyModel.setValType(valType);
        rdtRelyModel.setUnknownType(unknownType);

        //加载target class
        for (Class targetClass : targetClassValueMap.keySet()) {
            loadClassWithAnnotation(targetClass);
        }
    }

    public ClassModel getClassModel(Class currentClass) {
        Map<Class, ClassModel> classModelMap = properties.getClassModelMap();
        ClassModel classModel = classModelMap.get(currentClass);
        if (classModel == null) {
            classModel = new ClassModel();
            classModel.setClassName(currentClass.getName());
            classModel.setSimpleName(currentClass.getSimpleName());
            classModel.setCurrentClass(currentClass);
            classModel.setBaseClass(rdtResolver.isBaseClass(currentClass));
            classModel.setBuilderMark(0);
            if (classModel.getBaseClass()) {
                //必须有空的构造方法
                try {
                    currentClass.newInstance();
                } catch (Exception e) {
                    /*if (e instanceof InstantiationException || e instanceof IllegalAccessException) {
                    }*/
                    throw new IllegalArgumentException(classModel.getClassName() + " is base class, but unable to create instance by newInstance()");
                }

                if (properties.getEnableEntityName()) {
                    classModel.setEntityName(rdtResolver.getEntityName(currentClass));
                }
            }
            classModel.setFieldList(rdtResolver.getFields(currentClass));

            //fill未找到期望数据个数时的提示
            RdtEntityTips rdtEntityTips = rdtResolver.getAnnotation(currentClass, RdtEntityTips.class);
            if (rdtEntityTips != null) {
                classModel.setNotFoundTips(rdtResolver.getTipsContent(rdtEntityTips.notFound()));
                classModel.setNotFoundMoreTips(rdtResolver.getTipsContent(rdtEntityTips.notFoundMore()));
            }

            classModelMap.put(currentClass, classModel);
        }
        return classModel;
    }

    public void initColumn(ClassModel classModel, Field field) {
        Map<String, Column> propertyColumnMap = classModel.getPropertyColumnMap();  //属性对应的column信息
        String propertyName = field.getName();
        Column column = propertyColumnMap.get(propertyName);
        if (column == null) {
            column = new Column();
            column.setEntityClass(classModel.getCurrentClass());
            column.setProperty(propertyName);
            column.setField(field);
            classModel.getPropertyFieldMap().put(propertyName, field);

            if (properties.getEnableColumnName()) {
                column.setName(rdtResolver.getColumnName(classModel.getCurrentClass(), field));
            }
            column.setPropertyClass(PojoUtils.getFieldClass(field));
            column.setAlias(rdtResolver.getPropertyAlias(field, propertyName));  //alias
            column.setIsTransient(rdtResolver.isColumnTransient(classModel, field));
            column.setIsPrimaryId(false);
            //验证别名唯一性
            Map<String, String> aliasPropertyMap = classModel.getAliasPropertyMap();  //该类属性别名对应的该类属性名称
            String alias = column.getAlias();
            String aliasProperty = aliasPropertyMap.get(alias);
            if (StringUtils.isEmpty(aliasProperty)) {
                aliasPropertyMap.put(column.getAlias(), propertyName);
            } else {
                throw new IllegalArgumentException(classModel.getClassName() + " property " + propertyName + "can't to alias named " + alias + "," +
                        " because " + aliasProperty + " has named this alias");
            }

            //获取nullTips
            RdtConditionTips tips = rdtResolver.getAnnotation(field, RdtConditionTips.class);
            if (tips != null) {
                column.setNotAllowedNullTips(rdtResolver.getTipsContent(tips.nullTips()));
            }
            propertyColumnMap.put(propertyName, column);
        }
    }

    /**
     * 加载当前classModel的field的列数据
     *
     * @param classModel
     * @param field
     */
    public Column getColumn(ClassModel classModel, Field field) {
        initColumn(classModel, field);
        Map<String, Column> propertyColumnMap = classModel.getPropertyColumnMap();
        return propertyColumnMap.get(field.getName());
    }

    /**
     * 查找classModel中指定别名的列信息
     * @param classModel
     * @param fieldAlias 别名名称
     * @return
     */
    public Column getColumn(ClassModel classModel, String fieldAlias) {
        return getColumn(classModel, fieldAlias, true);
    }

    /**
     * 获取classModel的指定列
     * @param classModel
     * @param param 参数 别名/属性名称
     * @param alias 是否为别名
     * @return
     */
    public Column getColumn(ClassModel classModel, String param, boolean alias) {
        String property;
        if (alias) {
            Map<String, String> aliasPropertyMap = classModel.getAliasPropertyMap();  //该类属性别名对应的该类属性名称
            property = aliasPropertyMap.get(param);
        } else property = param;

        if (alias && StringUtils.isNotBlank(property)) {
            //通过别名获取时如果property存在说明已初始化过对应的column
            return classModel.getPropertyColumnMap().get(property);
        }

        for (Field field : classModel.getFieldList()) {
            //获取当前field对应的column信息
            Column column = getColumn(classModel, field);
            if (alias) {
                if (column.getAlias().equals(param)) return column;
            } else {
                if (column.getProperty().equals(param)) return column;
            }
        }
        String msg;
        if (alias) msg = classModel.getClassName() + " has no property alias named " + param + "";
        else msg = classModel.getClassName() + " has no property named " + param + "";
        throw new IllegalArgumentException(msg);
    }


    /**
     * 获取classModel中target class指定索引的modifyDescribe
     * @param classModel
     * @param targetClassModel
     * @param index
     * @return
     */
    private ModifyDescribe getModifyDescribe(ClassModel classModel, ClassModel targetClassModel, int index) {
        //@RdtField及@RdtFieldCondition中的target应该为base class
        if (!targetClassModel.getBaseClass()) throw new IllegalArgumentException(classModel.getClassName() + " field target class " + targetClassModel.getClassName()
                + "and index " + index + " must be base class");
        Class targetClass = targetClassModel.getCurrentClass();

        //处理当前类与target class的关系
        judgeClassRelation(classModel, targetClassModel);

        Map<Class, List<ModifyDescribe>> targetClassModifyDescribeMap = classModel.getTargetClassModifyDescribeMap();
        List<ModifyDescribe> modifyDescribeList = targetClassModifyDescribeMap.get(targetClass);
        if (modifyDescribeList == null) {
            modifyDescribeList = new ArrayList<ModifyDescribe>();
            targetClassModifyDescribeMap.put(targetClass, modifyDescribeList);
        }

        int position = 0;

        ModifyDescribe modifyDescribe = null;
        for (ModifyDescribe current : modifyDescribeList) {
            if (current.getIndex() == index) {
                modifyDescribe = current;
                break;
            } else if (current.getIndex() > index) break;
            position ++;
        }

        if (modifyDescribe == null) {
            modifyDescribe = new ModifyDescribe();
            modifyDescribe.setEntityClass(classModel.getCurrentClass());
            modifyDescribe.setTargetClass(targetClass);
            modifyDescribe.setIndex(index);
            modifyDescribeList.add(position, modifyDescribe);
        }
        return modifyDescribe;
    }



    /**
     * 获取classModel中target class指定索引的modifyRelyDescribe
     * @param classModel
     * @param targetClassModel
     * @param index
     * @return
     */
    private ModifyRelyDescribe getModifyRelyDescribe(ClassModel classModel, ClassModel targetClassModel, Column relyColumn, int index, int group) {
        //@RdtField及@RdtFieldCondition中的target应该为base class
        if (!targetClassModel.getBaseClass()) throw new IllegalArgumentException(classModel.getClassName() + " field target class " + targetClassModel.getClassName()
                + "and index " + index + " must be base class");
        Class targetClass = targetClassModel.getCurrentClass();

        //处理当前类与target class的关系
        judgeClassRelation(classModel, targetClassModel);

        //column key 为所依赖的列, integer key 对应的为 group
        Map<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>> targetClassModifyRelyDescribeMap = classModel.getTargetClassModifyRelyDescribeMap();

        Map<Column, Map<Integer, List<ModifyRelyDescribe>>> modifyRelyDescribeMap = targetClassModifyRelyDescribeMap.get(targetClass);

        if (modifyRelyDescribeMap == null) {
            modifyRelyDescribeMap = new LinkedHashMap<Column, Map<Integer, List<ModifyRelyDescribe>>>();
            targetClassModifyRelyDescribeMap.put(targetClass, modifyRelyDescribeMap);
        }

        Map<Integer, List<ModifyRelyDescribe>> groupDescribeMap = modifyRelyDescribeMap.get(relyColumn);

        if (groupDescribeMap == null) {
            groupDescribeMap = new HashMap<Integer, List<ModifyRelyDescribe>>();
            modifyRelyDescribeMap.put(relyColumn, groupDescribeMap);
        }

        List<ModifyRelyDescribe> modifyDescribeList = groupDescribeMap.get(group);

        if (modifyDescribeList == null) {
            modifyDescribeList = new ArrayList<ModifyRelyDescribe>();
            groupDescribeMap.put(group, modifyDescribeList);
        }

        int position = 0;

        ModifyRelyDescribe modifyDescribe = null;
        for (ModifyRelyDescribe current : modifyDescribeList) {
            if (current.getIndex() == index) {
                modifyDescribe = current;
                break;
            } else if (current.getIndex() > index) break;
            position ++;
        }

        if (modifyDescribe == null) {
            modifyDescribe = new ModifyRelyDescribe();
            modifyDescribe.setEntityClass(classModel.getCurrentClass());
            modifyDescribe.setTargetClass(targetClass);
            modifyDescribe.setRelyColumn(relyColumn);
            modifyDescribe.setIndex(index);
            modifyDescribe.setGroup(group);
            findModifyDescribeRelyValue(classModel, targetClassModel, relyColumn, group, modifyDescribe);
            modifyDescribeList.add(position, modifyDescribe);
        }
        return modifyDescribe;
    }

    //设置modifyRelyDescribe当前所依赖列字段为targetClassModel时的值及值类型
    private void findModifyDescribeRelyValue(ClassModel classModel, ClassModel targetClassModel, Column relyColumn, int group, ModifyRelyDescribe modifyDescribe) {
        Map<String, Map<Integer, RdtRelyModel>> propertyRelyDataMap = classModel.getPropertyRelyDataMap();
        Map<Integer, RdtRelyModel> groupModelMap = propertyRelyDataMap.get(relyColumn.getProperty());
        RdtRelyModel rdtRelyModel = groupModelMap.get(group);

        Map<Class, KeyTargetModel> targetClassValueMap = rdtRelyModel.getTargetClassValueMap();
        Class targetClass = targetClassModel.getCurrentClass();
        KeyTargetModel keyTargetModel = targetClassValueMap.get(targetClass);

        modifyDescribe.setValType(rdtRelyModel.getValType());
        modifyDescribe.setValList(keyTargetModel.getValueList());
        modifyDescribe.setRdtRelyModel(rdtRelyModel);
        modifyDescribe.setUpdateIgnoresValList(keyTargetModel.getUpdateIgnoresValueList());
        modifyDescribe.setNotAllowedTypeTips(keyTargetModel.getNotAllowedTypeTips());

        //如果target class等于 unknown type
        if (targetClass.equals(rdtRelyModel.getUnknownType())) {
            modifyDescribe.setNotInValList(rdtRelyModel.getUnknownNotExistValues());
        }
    }


    private ModifyColumn getModifyColumn(Column column, Column targetColumn) {
        ModifyColumn modifyColumn = new ModifyColumn();

        modifyColumn.setColumn(column);
        modifyColumn.setTargetColumn(targetColumn);

        if (!column.getIsTransient()) {
            //设置target class被使用的字段
            ClassModel targetModel = getClassModel(targetColumn.getEntityClass());
            targetModel.addUsedProperty(targetColumn.getProperty());
        }
        return modifyColumn;
    }

    private ModifyCondition getModifyCondition(Column column, Column targetColumn) {
        ModifyCondition modifyCondition = new ModifyCondition();
        if (column.getIsTransient()) {
            //warn
            logger.warn(column.getEntityClass().getName() + " property " + column.getProperty() + " as condition column is transient, please make sure no problem.");
        }
        modifyCondition.setColumn(column);
        modifyCondition.setTargetColumn(targetColumn);
        return modifyCondition;
    }


    /**
     * 处理关系,将当前class设置为target class的处理类的类型
     * @param classModel
     * @param targetClassModel
     */
    private void judgeClassRelation(ClassModel classModel, ClassModel targetClassModel) {
        if (classModel.getBaseClass()) {  //classModel为基本类时,则作为targetClass数据变化时要处理的类
            targetClassModel.getChangedRelaxedClassSet().add(classModel.getCurrentClass());
        } else {
            targetClassModel.getChangedComplexClassSet().add(classModel.getCurrentClass());
        }
    }

    private ComplexModel builderComplexModelConfigData(ClassModel classModel, Column column, boolean one) {
        Class propertyClass = column.getPropertyClass();
        ComplexModel complexModel = new ComplexModel();
        complexModel.setIsOne(one);

        Class current = rdtResolver.getRelationModelCurrentClassType(classModel, column, one); //获取类型
        complexModel.setCurrentType(current);

        if (current.isPrimitive()|| Date.class.isAssignableFrom(current)
                || String.class.isAssignableFrom(current)
                || Number.class.isAssignableFrom(current)
                || Boolean.class.isAssignableFrom(current)
                || Character.class.isAssignableFrom(current)
                ) {
            throw new IllegalArgumentException(classModel.getClassName() + " property " + column.getProperty() + " type is " + current.getName() + ", it's not allowed association object type");
        }

        complexModel.setOwnerType(classModel.getCurrentClass());  //所属类
        complexModel.setOwnerBase(classModel.getBaseClass());
        complexModel.setProperty(column.getProperty());
        complexModel.setPropertyType(propertyClass);
        complexModel.setColumn(column.getName());
        complexModel.setAlias(column.getAlias());

        classModel.getComplexModelList().add(complexModel);

        Map<Class, List<ComplexModel>> complexModelsMap = properties.getClassComplexModelsMap();

        //存储current所对应的多个关联信息
        List<ComplexModel> relationModelList = complexModelsMap.get(current);
        if (relationModelList == null) {
            relationModelList = new ArrayList<ComplexModel>();
            complexModelsMap.put(current, relationModelList);
        }
        relationModelList.add(complexModel);

        loadClassWithAnnotation(current);  //加载不在packge的class
        return complexModel;
    }


    /**
     * 验证当前column与target column的格式,比较条件时类型需一致
     * @param column
     * @param targetColumn
     * @param classModel
     * @param targetClassModel
     * @param condition  是否为条件field
     */
    private void columnCompareVerification(Column column, Column targetColumn, ClassModel classModel, ClassModel targetClassModel, boolean condition) {

        rdtResolver.columnCompareVerification(column, targetColumn, classModel, targetClassModel, condition, properties.getIsTargetColumnNotTransient(), properties.getIsModifyColumnMustSameType());
    }

    /**
     * 加载注解上的target class
     * @param currentClass
     */
    private void loadClassWithAnnotation(Class currentClass) {
        Set<Class> extraClassSet = properties.getExtraClassSet();
        builderClass(currentClass);
        if (!properties.hasPackageContainsClass(currentClass) && !extraClassSet.contains(currentClass)) {
            extraClassSet.add(currentClass);
        }
    }
}
