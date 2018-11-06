package com.devloper.joker.redundant.builder;

import com.devloper.joker.redundant.annotation.RdtComplex;
import com.devloper.joker.redundant.annotation.RdtMany;
import com.devloper.joker.redundant.annotation.RdtOne;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.field.RdtFieldConditions;
import com.devloper.joker.redundant.annotation.field.RdtFields;
import com.devloper.joker.redundant.annotation.rely.*;
import com.devloper.joker.redundant.model.*;
import com.devloper.joker.redundant.model.commons.RdtRelyModel;
import com.devloper.joker.redundant.model.commons.RdtRelyTargetColumnModel;
import com.devloper.joker.redundant.resolver.RdtResolver;
import com.devloper.joker.redundant.utils.ClassUtils;
import com.devloper.joker.redundant.utils.PojoUtils;
import org.apache.commons.lang.StringUtils;
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
        ClassModel classModel = getClassModel(currentClass);

        if (classModel.getBuilderMark() != 0) {
            return;
        }
        classModel.setBuilderMark(1);

        Boolean isBaseClass = classModel.getBaseClass();

        List<Field> fieldList = classModel.getFieldList();
        List<Class<? extends Annotation>> sortAnnotationClassList = Arrays.asList(
                RdtFieldConditions.class, RdtFields.class,
                RdtFieldCondition.class, RdtField.class,
                RdtRelys.class, RdtRely.class,
                RdtFieldConditionRely.class, RdtFieldRely.class,
                RdtOne.class, RdtMany.class, RdtComplex.class);

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
                if (describe.getConditionList().isEmpty() && !describe.getColumnList().isEmpty()) {
                    logger.warn("{} target about [{} index ({})] has no condition, please make sure no problem.", classModel.getClassName(), targetClass.getName(), describe.getIndex());
                }
            }
        }
        Map<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>> targetClassModifyRelyDescribeMap = classModel.getTargetClassModifyRelyDescribeMap();

        for (Class targetClass : targetClassModifyRelyDescribeMap.keySet()) {
            Map<Column, Map<Integer, List<ModifyRelyDescribe>>> relyColumnDataMap = targetClassModifyRelyDescribeMap.get(targetClass);
            if (relyColumnDataMap == null) continue;
            classModel.getTargetRelyModifyClassSet().add(targetClass);

            for (Column relyColumn : relyColumnDataMap.keySet()) {
                Map<Integer, List<ModifyRelyDescribe>> groupDataMap = relyColumnDataMap.get(relyColumn);
                if (groupDataMap == null) continue;
                for (Integer group : groupDataMap.keySet()) {
                    List<ModifyRelyDescribe> describeList = groupDataMap.get(group);
                    for (ModifyRelyDescribe describe : describeList) {
                        if (describe.getConditionList().isEmpty() && !describe.getColumnList().isEmpty()) {
                            logger.warn("{} rely column {}({}) group ({}) target about [{} index ({})] has no condition, please make sure no problem.", classModel.getClassName(), relyColumn.getProperty(), relyColumn.getPropertyClass().getName(), group, targetClass.getName(), describe.getIndex());
                        }
                    }
                }
            }
        }
        classModel.setBuilderMark(2);
    }

    /**
     * 将依赖信息进行转换成对应的数据,若依赖列的group索引未被列引用不会出现该索引的配置
     * @param classModel
     * @param condition
     */
    private void builderModifyRelyDescribe(ClassModel classModel, boolean condition) {
        Map<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>> propertyTargetMap;

        if (condition) propertyTargetMap = classModel.getPropertyTargetConditionRelyMap();
        else propertyTargetMap = classModel.getPropertyTargetRelyMap();

        for (String property : propertyTargetMap.keySet()) {
            Column currentColumn = getColumn(classModel, property, false);//当前列信息

            Map<Column, Map<Integer, RdtRelyTargetColumnModel>> targetRelyMap = propertyTargetMap.get(property);
            if (targetRelyMap != null) {
                for(Column relyColumn : targetRelyMap.keySet()) {
                    Map<Integer, RdtRelyTargetColumnModel> indexRelyMap = targetRelyMap.get(relyColumn);
                    if (indexRelyMap != null) {
                        for (Integer index : indexRelyMap.keySet()) {
                            RdtRelyTargetColumnModel columnModel = indexRelyMap.get(index);
                            Integer group = columnModel.getGroup(); //所属group
                            Map<Class, Column> classColumnMap = columnModel.getClassTargetColumnMap(); //class类型所对应的列

                            if (classColumnMap != null) {
                                for (Class targetClass : classColumnMap.keySet()) {
                                    Column targetColumn = classColumnMap.get(targetClass);
                                    ModifyRelyDescribe modifyRelyDescribe = getModifyRelyDescribe(classModel, getClassModel(targetClass), relyColumn, index, group);

                                    if (condition) {
                                        ModifyCondition modifyCondition = getModifyCondition(currentColumn, targetColumn);
                                        modifyRelyDescribe.getConditionList().add(modifyCondition);
                                    } else {
                                        ModifyColumn modifyColumn = getModifyColumn(currentColumn, targetColumn);
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
                RdtRelys rdtAnnotation = (RdtRelys) annotation;
                for (RdtRely rdtRely : rdtAnnotation.value()) {
                    builderRdtRelyConfigData(classModel, column, rdtRely);
                }
            } else if (annotation instanceof RdtFieldConditionRely) {
                builderPropertyRelyGroupConfigData(classModel, column, annotation);
            } else if (annotation instanceof RdtFieldRely) {
                builderPropertyRelyGroupConfigData(classModel, column, annotation);
            }
        }
    }

    private void builderRdtFieldConfigData(ClassModel classModel, Column column, RdtField rdtAnnotation) {
        Class targetClass = rdtAnnotation.target();  //当前属性要修改所对应的class
        loadExtraClass(targetClass);

        String targetProperty = rdtAnnotation.property();  //要修改属性的对应的别名
        targetProperty = StringUtils.isEmpty(targetProperty) ? column.getProperty() : targetProperty;
        int index = rdtAnnotation.index();

        //获取target class对应的列
        ClassModel targetClassModel = getClassModel(targetClass);
        Column targetColumn = getColumn(targetClassModel, targetProperty);

        columnCompareVerification(column, targetColumn, classModel, targetClassModel, false);  //验证条件的属性类型一致

        //添加修改属性内容
        ModifyDescribe modifyDescribe = getModifyDescribe(classModel, targetClassModel, index);
        modifyDescribe.getColumnList().add(getModifyColumn(column, targetColumn));
    }

    private void builderRdtFieldConditionConfigData(ClassModel classModel, Column column, RdtFieldCondition rdtAnnotation) {
        Class targetClass = rdtAnnotation.target();  //对应的class
        loadExtraClass(targetClass);

        String targetProperty = rdtAnnotation.property();  //对应的别名
        targetProperty = StringUtils.isEmpty(targetProperty) ? column.getProperty() : targetProperty;
        int index = rdtAnnotation.index();

        //获取target class对应的列
        ClassModel targetClassModel = getClassModel(targetClass);
        Column targetColumn = getColumn(targetClassModel, targetProperty);
        columnCompareVerification(column, targetColumn, classModel, targetClassModel, true);  //验证属性类型一致

        //添加修改条件
        ModifyDescribe modifyDescribe = getModifyDescribe(classModel, targetClassModel, index);
        modifyDescribe.getConditionList().add(getModifyCondition(column, targetColumn));
    }


    /**
     * 解析@RdtFieldConditionRely和@RdtFieldRely数据为对应字段不同group index下所依赖字段类型值对应class所使用的属性列
     * @param classModel
     * @param column
     * @param annotation
     */
    private void builderPropertyRelyGroupConfigData(ClassModel classModel, Column column, Annotation annotation) {
        boolean conditionRely = false;
        int group;
        int relyIndex;
        String[] targetPropertys;
        String nullTypeProperty;
        String unknowTypeProperty;

        String relyProperty;  //依赖的字段别名
        if (annotation instanceof RdtFieldConditionRely) {
            conditionRely = true;
            RdtFieldConditionRely rdt = (RdtFieldConditionRely) annotation;
            group = rdt.group();
            relyIndex = rdt.index();
            relyProperty = rdt.property();

            targetPropertys = rdt.targetPropertys();
            nullTypeProperty = StringUtils.isNotBlank(rdt.nullTypeProperty()) ? rdt.nullTypeProperty() : null;
            unknowTypeProperty = StringUtils.isNotBlank(rdt.unknowTypeProperty()) ? rdt.unknowTypeProperty() : null;

        } else if (annotation instanceof RdtFieldRely) {
            RdtFieldRely rdt = (RdtFieldRely) annotation;
            group = rdt.group();
            relyIndex = rdt.index();
            relyProperty = rdt.property();
            targetPropertys = rdt.targetPropertys();
            nullTypeProperty = StringUtils.isNotBlank(rdt.nullTypeProperty()) ? rdt.nullTypeProperty() : null;
            unknowTypeProperty = StringUtils.isNotBlank(rdt.unknowTypeProperty()) ? rdt.unknowTypeProperty() : null;
        } else throw new IllegalArgumentException("not support this type");

        String property = column.getProperty();
        Column relyColumn = getColumn(classModel, relyProperty);

        String hintPrefix = classModel.getClassName() + " property " + property + " field ";
        if (conditionRely) hintPrefix += "condition ";
        hintPrefix += "rely config :" + " group index " + group + " has error, caused by :";

        Map<Integer, RdtRelyModel> relyDataMap = classModel.getPropertyRelyDataMap().get(relyColumn.getProperty());
        RdtRelyModel rdtRelyModel = null;
        if (relyDataMap != null) rdtRelyModel = relyDataMap.get(group);
        //需要有对应的rely model数据
        if (rdtRelyModel == null) throw new IllegalArgumentException(hintPrefix +  " corresponding rely data config named " +  relyColumn.getProperty() + " not exist");
        Map<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>> propertyTargetConditionRelyMap;
        if (conditionRely) {
            propertyTargetConditionRelyMap = classModel.getPropertyTargetConditionRelyMap();
        } else {
            propertyTargetConditionRelyMap = classModel.getPropertyTargetRelyMap();
        }
        Map<Column, Map<Integer, RdtRelyTargetColumnModel>> relyColumnGroupModelMap = propertyTargetConditionRelyMap.get(property);
        if (relyColumnGroupModelMap == null) {
            relyColumnGroupModelMap = new LinkedHashMap<Column, Map<Integer, RdtRelyTargetColumnModel>>();
            propertyTargetConditionRelyMap.put(property, relyColumnGroupModelMap);
        }

        Map<Integer, RdtRelyTargetColumnModel> currentDataMap = relyColumnGroupModelMap.get(relyColumn);
        if (currentDataMap == null) {
            currentDataMap = new HashMap<Integer, RdtRelyTargetColumnModel>();
            relyColumnGroupModelMap.put(relyColumn, currentDataMap);
        }
        RdtRelyTargetColumnModel model = currentDataMap.get(relyIndex);
        if (model != null) throw new IllegalArgumentException(hintPrefix + " already exist");

        List<Class> keyTargetClassList = rdtRelyModel.getKeyTargetClassList();
        model = new RdtRelyTargetColumnModel();
        currentDataMap.put(relyIndex, model);
        model.setGroup(group);

        //获取各个class所使用的column对象
        Map<Class, Column> classTargetColumnMap = model.getClassTargetColumnMap();
        int targetEqPropertysLength = targetPropertys.length;
        if (targetEqPropertysLength > 1 && keyTargetClassList.size() != targetEqPropertysLength) {
            throw new IllegalArgumentException(hintPrefix + " should setting propertys according the corresponding rely data config target class order and size is eq");
        }

        List<String> usePropertyList = new ArrayList<String>();
        if (targetEqPropertysLength == 0 && nullTypeProperty == null
                && unknowTypeProperty == null) {
            usePropertyList.add(property);
        } else {
            for (String current : targetPropertys) {
                if (StringUtils.isEmpty(current)) {
                    throw new IllegalArgumentException(hintPrefix + " target property not allowed empty");
                }
                usePropertyList.add(current);
            }
        }
        int index = 0;
        for (Class targetClass : keyTargetClassList) {
            String alias;
            if (usePropertyList.size() == 1) alias = usePropertyList.get(0);
            else alias = usePropertyList.get(index);
            builderPropertyRelyGroupTypeData(classModel, column, conditionRely, targetClass, alias, classTargetColumnMap, hintPrefix, null);
            index ++;
        }

        Class nullType = rdtRelyModel.getNullType();
        if (nullType != null && StringUtils.isEmpty(nullTypeProperty) && usePropertyList.size() == 1) {
            nullTypeProperty = usePropertyList.get(0);
        }
        Class unknowType = rdtRelyModel.getUnknowType();
        if (unknowType != null && StringUtils.isEmpty(unknowTypeProperty) && usePropertyList.size() == 1) {
            unknowTypeProperty = usePropertyList.get(0);
        }

        //处理nullType
        builderPropertyRelyGroupTypeData(classModel, column, conditionRely, nullType, nullTypeProperty, classTargetColumnMap, hintPrefix, " null type appointed");
        //处理unknowType
        builderPropertyRelyGroupTypeData(classModel, column, conditionRely, rdtRelyModel.getUnknowType(), unknowTypeProperty, classTargetColumnMap, hintPrefix, " unknow type appointed");
    }

    private void builderPropertyRelyGroupTypeData(ClassModel classModel, Column column, boolean conditionRely, Class type, String typePropertyAlias, Map<Class, Column> classTargetColumnMap, String hintPrefix, String describe) {
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
                        columnCompareVerification(column, currentColumn, classModel, typeClassModel, conditionRely);
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

    /**
     * 解析@RdtRely数据
     * @param classModel
     * @param column
     * @param rdtRely
     */
    private void builderRdtRelyConfigData(ClassModel classModel, Column column, RdtRely rdtRely) {
        int group = rdtRely.group();
        String property = column.getProperty();
        String hintPrefix = classModel.getClassName() + " property " + property + " rely data config :" + " group index " + group + " has error, caused by :";

        Map<String, Map<Integer, RdtRelyModel>> propertyRelyDataMap = classModel.getPropertyRelyDataMap();
        Map<Integer, RdtRelyModel> currentRelyDataMap = propertyRelyDataMap.get(property);
        if (currentRelyDataMap == null) {
            currentRelyDataMap = new HashMap<Integer, RdtRelyModel>();
            propertyRelyDataMap.put(property, currentRelyDataMap);
        }
        RdtRelyModel rdtRelyModel = currentRelyDataMap.get(group);
        if (rdtRelyModel != null) throw new IllegalArgumentException(hintPrefix + " already exist");
        rdtRelyModel = new RdtRelyModel();
        currentRelyDataMap.put(group, rdtRelyModel);

        Class propertyClass = column.getPropertyClass();
        boolean propertyClassIsEnum = propertyClass.isEnum();
        Class valType = rdtRely.valType() == Void.class ? propertyClass : rdtRely.valType();
        if (propertyClassIsEnum) {
            if (valType != int.class && valType != String.class && valType != propertyClass) {
                throw new IllegalArgumentException(hintPrefix +
                        " type belong to enum, should by default or designate use ordinal type - int.class or name type - String.class");
            }
        }

        if (ClassUtils.familyClass(valType, Collection.class)) throw  new IllegalArgumentException(hintPrefix +
                " type not support collection type");

        Class unknowType = rdtRely.unknowType() == Void.class ? null : rdtRely.unknowType();
        Class nullType = rdtRely.nullType() == Void.class ? null : rdtRely.nullType();

        if (nullType != null) { //值类型为基本数据类型时却使用null type时
            if (valType.isPrimitive()) throw new IllegalArgumentException(hintPrefix +
                    " type is primitive, not has null value type");
        }

        Map<Class, List<Object>> targetClassValueMap = rdtRelyModel.getTargetClassValueMap();


        Map<Object, Class> typeValClassMap = new HashMap<Object, Class>(16);
        for (KeyTarget keyTarget : rdtRely.value()) {
            Class target = keyTarget.target();
            List<Object> valList = targetClassValueMap.get(target);
            if (valList != null) {
                //target class只允许出现一次,通过value配置相关类型值
                throw new IllegalArgumentException(hintPrefix +
                        " @KeyTarget target type " + target.getName() + " only allowed setting once");
            }

            valList = new ArrayList<Object>();
            targetClassValueMap.put(target, valList);

            rdtRelyModel.getKeyTargetClassList().add(target);
            //解析val
            String[] stringArrays = keyTarget.value();
            for (String current : stringArrays) {
                if (StringUtils.isEmpty(current)) continue;
                Object val = null;
                if (valType == String.class) val = current;
                else {
                    if (valType.isEnum()) {
                        //解析枚举对应的值
                        String currentName;
                        if (current.matches("\\d+")) {
                            currentName = "ordinal";
                        } else {
                            currentName = "name";
                        }
                        boolean hasMatch = false;
                        for (Object constant : valType.getEnumConstants()) {
                            if (current.equals(rdtResolver.getPropertyValue(constant, currentName).toString())) {
                                val = constant;
                                hasMatch = true;
                                break;
                            }
                        }
                        if (!hasMatch) {
                            throw new IllegalArgumentException(hintPrefix + " @KeyTarget target type " + target.getName() + " has no enum " + valType.getName() + " type val "+ current);
                        }

                    } else {
                        val = rdtResolver.cast(current, valType);
                    }
                }

                Class typeClass = typeValClassMap.get(val);
                if (typeClass != null) {
                    throw new IllegalArgumentException(hintPrefix + " @KeyTarget target type " + target.getName() + " type val "+ val + " has already exist the before target type " + typeClass.getName());
                } else {
                    //将此类型值与class绑定
                    typeValClassMap.put(val, target);
                    valList.add(val);
                }

            }
        }

        if (nullType != null) { //最后处理nullType
            List<Object> valList = targetClassValueMap.get(nullType);
            if (valList == null) {
                valList = new ArrayList<Object>();
                targetClassValueMap.put(nullType, valList);
            }
            valList.add(null);
        }

        if (unknowType != null) {
            loadExtraClass(unknowType);
            List<Object> unknowNotExistValues = rdtRelyModel.getUnknowNotExistValues();
            for (List<Object> values : targetClassValueMap.values()) {
                unknowNotExistValues.addAll(values);
            }
            unknowNotExistValues.removeAll(rdtRelyModel.getTargetClassValueMap().get(unknowType));
        }

        rdtRelyModel.setValType(valType);
        rdtRelyModel.setUnknowType(unknowType);
        rdtRelyModel.setNullType(nullType);

        for (Class current : targetClassValueMap.keySet()) {
            loadExtraClass(current);
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
            if (StringUtils.isEmpty(aliasProperty))
                aliasPropertyMap.put(column.getAlias(), propertyName);
            else
                throw new IllegalArgumentException(classModel.getClassName() + " property " + propertyName + "can't to alias named " + alias + "," +
                        " because " + aliasProperty + " has named this alias");

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

        Map<Class, List<Object>> targetClassValueMap = rdtRelyModel.getTargetClassValueMap();

        Class targetClass = targetClassModel.getCurrentClass();
        modifyDescribe.setValType(rdtRelyModel.getValType());
        modifyDescribe.setValList(targetClassValueMap.get(targetClass));
        modifyDescribe.setRdtRelyModel(rdtRelyModel);
        if (targetClass.equals(rdtRelyModel.getUnknowType())) {
            modifyDescribe.setUnknowNotExistValList(rdtRelyModel.getUnknowNotExistValues());
        }
    }



    private ModifyColumn getModifyColumn(Column column, Column targetColumn) {
        ModifyColumn modifyColumn = new ModifyColumn();

        modifyColumn.setColumn(column);
        modifyColumn.setTargetColumn(targetColumn);

        //设置target class被使用的字段
        getClassModel(PojoUtils.getFieldLocalityClass(modifyColumn.getTargetColumn().getField())).getUsedPropertySet().add(modifyColumn.getTargetColumn().getProperty());
        return modifyColumn;
    }

    private ModifyCondition getModifyCondition(Column column, Column targetColumn) {
        ModifyCondition modifyCondition = new ModifyCondition();
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

        loadExtraClass(current);  //加载不在packge的class
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

        rdtResolver.columnCompareVerification(column, targetColumn, classModel, targetClassModel, condition, properties.getIsTargetColumnNotTransient());
    }

    /**
     * 加载不在packge的class
     * @param currentClass
     */
    private void loadExtraClass(Class currentClass) {
        Set<Class> extraClassSet = properties.getExtraClassSet();
        if (!properties.hasPackageContainsClass(currentClass) && !extraClassSet.contains(currentClass)) {
            extraClassSet.add(currentClass);
            builderClass(currentClass);
        }
    }
}
