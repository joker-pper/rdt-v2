package com.joker17.redundant.core;

import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.annotation.RdtId;
import com.joker17.redundant.annotation.base.RdtBaseEntity;
import com.joker17.redundant.annotation.base.RdtBaseField;
import com.joker17.redundant.model.*;
import com.joker17.redundant.support.Prototype;
import com.joker17.redundant.utils.*;
import com.joker17.redundant.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class RdtResolver {

    protected final Logger logger = LoggerFactory.getLogger(RdtResolver.class);

    private List<Class> baseEntityAnnotationClassList = null;
    private List<Class> primaryIdAnnotationClassList = null;
    private List<Class> columnTransientAnnotationClassList = null;

    /**
     * 哪些作为修改基本类的注解
     * @return
     */
    public final List<Class> getBaseEntityAnnotationClassList() {
        if (baseEntityAnnotationClassList == null) {
            List<Class> results = new ArrayList<Class>();
            results.add(defaultBaseEntityAnnotation());
            Class<?>[] customEntityAnnotations = customBaseEntityAnnotations();
            if (customEntityAnnotations != null) {
                for (Class annotations : customEntityAnnotations) {
                    if (annotations == null) continue;
                    results.add(annotations);
                }
            }
            baseEntityAnnotationClassList = results;
        }

        return baseEntityAnnotationClassList;
    }

    protected final Class<?> defaultBaseEntityAnnotation() {
        return RdtBaseEntity.class;
    }

    /**
     * 定义作为基本类的注解
     * @return
     */
    protected abstract Class<?>[] customBaseEntityAnnotations();


    public List<Class> getClasses(String packageName) {
        return PackageClassUtils.getClassList(packageName);
    }

    /**
     * 当前类是否为修改基本类
     * @param entityClass
     * @return
     */
    public final boolean isBaseClass(Class entityClass) {
        boolean flag = false;
        for (Class annotation : getBaseEntityAnnotationClassList()) {  //是否存在指定注解
            flag = getAnnotation(entityClass, annotation) != null;
            if (flag) break;
        }
        if (!flag) flag = isBaseClassByAnalysis(entityClass);
        return flag;
    }

    protected abstract boolean isBaseClassByAnalysis(Class entityClass);

    /**
     * 当开启获取列名时获取列名(通过注解属性获取值,优先级@Rdt(BaseField|Field)>@Column>@Field),如果未获取到可通过解析方法进行获取
     *
     * @param entityClass
     * @param field
     * @return
     */
    public String getColumnName(Class<?> entityClass, Field field) {
        String columnName = getColumnNameByAnnotations(entityClass, field);
        if (StringUtils.isEmpty(columnName)) columnName = getColumnNameByAnalysis(entityClass, field);
        return columnName;
    }

    /**
     * 通过注解获取列名
     *
     * @param entityClass
     * @param field
     * @return
     */
    protected String getColumnNameByAnnotations(Class<?> entityClass, Field field) {
        String columnName = null;
        String columnNameTemp = null;
        RdtBaseField rdtBaseField = getAnnotation(field, RdtBaseField.class);
        if (rdtBaseField != null) {
            columnNameTemp = rdtBaseField.columnName();
            if (!columnNameTemp.isEmpty()) {
                columnName = columnNameTemp;
            }
        }

        if (StringUtils.isEmpty(columnName)) {
            String[] annotations = new String[]{"javax.persistence.Column", "org.springframework.data.mongodb.core.mapping.Field"};
            String[] annotationPropertys = new String[]{"name", "value"};
            Method readMethod = null;

            for (int i = 0; i < annotations.length; ++i) {
                try {
                    Class fieldAnnotationClass = Class.forName(annotations[i]);
                    Annotation annotation = getAnnotation(field, fieldAnnotationClass);
                    if (annotation == null) {
                        if (readMethod == null) {
                            readMethod = PojoUtils.getReadMethod(field);
                        }
                        annotation = getAnnotation(readMethod, fieldAnnotationClass);
                    }

                    if (annotation != null) {
                        Object result = getAnnotationValue(annotation, annotationPropertys[i]);
                        if (result != null) {
                            columnNameTemp = (String) result;
                        }
                    }
                    if (columnNameTemp != null && !columnNameTemp.isEmpty()) {
                        columnName = columnNameTemp;
                        break;
                    }
                } catch (ClassNotFoundException e) {

                }
            }

        }
        return columnName;
    }

    protected abstract String getColumnNameByAnalysis(Class<?> entityClass, Field field);

    /**
     * 获取entityClass的表名
     * @param entityClass
     * @return
     */
    public final String getEntityName(Class<?> entityClass) {
        String entityName = getEntityNameByAnnotations(entityClass); //通过注解获取
        if (StringUtils.isEmpty(entityName)) entityName = getEntityNameByAnalysis(entityClass);
        return entityName;
    }

    protected String getEntityNameByAnnotations(Class<?> entityClass) {
        String entityName = null;
        RdtBaseEntity baseEntity = getAnnotation(entityClass, RdtBaseEntity.class);
        if (baseEntity != null) entityName = baseEntity.name();
        return entityName;
    }

    protected abstract String getEntityNameByAnalysis(Class<?> entityClass);


    //获取该属性的别名(如果存在@RdtBaseField注解则为alias不为空的值,反之则为propertyName）
    public final String getPropertyAlias(Field field, String propertyName) {
        RdtBaseField rdtBaseField = getAnnotation(field, RdtBaseField.class);
        String alias = propertyName;
        if (rdtBaseField != null) {
            String temp = rdtBaseField.alias();
            if (!temp.isEmpty()) {
                alias = temp;
            }
        }
        return alias;
    }

    /**
     * 获取当前类的id字段
     * @param entityClass
     * @return
     */
    public String getPrimaryId(Class entityClass, Field field) {
        if (primaryIdAnnotationClassList == null) {
            List<Class> classList = new ArrayList<Class>();
            Class<?>[] primaryIdAnnotations = primaryIdAnnotations();
            if (primaryIdAnnotations != null) {
                for (Class annotations : primaryIdAnnotations) {
                    if (annotations == null) continue;
                    classList.add(annotations);
                }
            }
            classList.add(RdtId.class);
            primaryIdAnnotationClassList = classList;
        }

        for (Class current : primaryIdAnnotationClassList) {
            if (getAnnotation(field, current) != null) {
                return field.getName();
            }
        }
        return getPrimaryIdByAnalysis(entityClass, field);
    }

    protected abstract Class<?>[] primaryIdAnnotations();

    protected abstract String getPrimaryIdByAnalysis(Class entityClass, Field field);

    /**
     * 数据库列字段是否存在
     * @param classModel
     * @param field
     * @return
     */
    public boolean isColumnTransient(ClassModel classModel, Field field) {
        if (columnTransientAnnotationClassList == null) {
            List<Class> classList = new ArrayList<Class>();
            Class<?>[] annotations = columnTransientAnnotations();
            if (annotations != null) {
                for (Class annotation : annotations) {
                    if (annotation == null) continue;
                    classList.add(annotation);
                }
            }
            columnTransientAnnotationClassList = classList;
        }

        Boolean result = false;
        for (Class current : columnTransientAnnotationClassList) {
            if (getAnnotation(field, current) != null) {
                result = true;
                break;
            }
        }

        return result;
    }

    //不存在数据库列的注解
    protected abstract Class<?>[] columnTransientAnnotations();

    /**
     * 获取当前列作为关联对象的class type
     * @param classModel
     * @param column
     * @param one
     * @return
     */
    public Class getRelationModelCurrentClassType(ClassModel classModel, Column column, boolean one) {
        Class type = column.getPropertyClass();
        Field field = classModel.getPropertyFieldMap().get(column.getProperty());
        String hint = classModel.getClassName() + " property " + column.getProperty() + " to get relation type with " + (one ? "@RdtOne" : "@RdtMany") + " has error, cause by : ";
        if (!one) {
            if (type.isArray()) {
                type = type.getComponentType();
            } else if (ClassUtils.familyClass(type, Collection.class)) {
                if (ClassUtils.familyClass(type, Map.class)) throw new IllegalArgumentException(hint + "the relation many type not support map");
                if (ClassUtils.getActualTypeArgumentsLength(field.getGenericType()) == 0) {
                    throw new IllegalArgumentException(hint + "the relation many type not has actual class type");
                }
                type = ClassUtils.getActualTypeArgumentClass(field, 0);
            }
        }
        if (type.isArray()) throw new IllegalArgumentException(hint + "the relation " + (one ? "one" : "many") +" actual class type must be not array");
        else if (ClassUtils.familyClass(type, Collection.class)) throw new IllegalArgumentException(hint + "the relation " + (one ? "one" : "many") +" actual class type must be not collection");
        return type;
    }


    /**
     * 验证当前column与target column的格式,比较条件时类型必须一致
     * @param column
     * @param targetColumn
     * @param classModel
     * @param targetClassModel
     * @param condition 是否为条件field
     * @param isTargetColumnNotTransient
     */
    public void columnCompareVerification(Column column, Column targetColumn, ClassModel classModel, ClassModel targetClassModel, boolean condition, boolean isTargetColumnNotTransient, boolean modifyColumnMustSameType) {
        Class currentEntityClass = column.getEntityClass();
        Class targetEntityClass = targetColumn.getEntityClass();
            /*String hint = targetEntityClass.getName() + " property " + targetColumn.getProperty()
                    + " type is " + targetColumn.getPropertyClass().getName() + ", " + currentEntityClass.getName() + " property " + column.getProperty() + " type is " +
                    column.getPropertyClass().getName();*/

        String hint = "rdt column --- " + currentEntityClass.getName() + " property " + column.getProperty() + "(" + column.getPropertyClass().getName() +
                ") ==> [" +  targetEntityClass.getName() + " property " + targetColumn.getProperty()
                + "(" + targetColumn.getPropertyClass().getName()   + ")]";

        if (isTargetColumnNotTransient && targetColumn.getIsTransient()) {
            throw new IllegalArgumentException(hint + " has error, cause by : target column must not transient.");
        }

        if (!column.getPropertyClass().equals(targetColumn.getPropertyClass())) {  //类型应一致
            if (condition) {
                throw new IllegalArgumentException(hint + " has error, cause by : the condition property type must consistent.");  //条件的类型必须一致
            } else {
                if (modifyColumnMustSameType) {
                    throw new IllegalArgumentException(hint + " has error, cause by : the property type must consistent.(RdtProperties property isModifyColumnMustSameType can setting allowed not eq type, but you should make sure can cast)");
                }
                //logger.warn(hint + ", please make sure can cast.");
            }
        }
    }


    public boolean isIgnoreModelClass(Class modelClass) {
        if (modelClass == null || modelClass.isInterface() || modelClass.isEnum() || modelClass.isAnnotation() || modelClass.isPrimitive()) {
            return true;
        }
        for (Class type : Arrays.asList(Map.class, Collection.class, Date.class, Number.class, String.class)) {
            if (ClassUtils.familyClass(modelClass, type)) {
                return true;
            }
        }

        return false;
    }

    protected String[] getOtherBasicClassStrArray() {
        return new String[] {"java.time.LocalDateTime"};
    }


    /**
     * 获取class是否为基础类型
     * @param current
     * @return
     */
    public boolean isBasicClass(Class current) {
        boolean result =
                (
                        current.isPrimitive()
                        || Date.class.isAssignableFrom(current)
                        || String.class.isAssignableFrom(current)
                        || Number.class.isAssignableFrom(current)
                        || Boolean.class.isAssignableFrom(current)
                        || Character.class.isAssignableFrom(current)
                );
        if (!result) {
            String[] otherClassStrArray = getOtherBasicClassStrArray();
            if (otherClassStrArray != null) {
                for (String otherClassStr : otherClassStrArray) {
                    try {
                        Class otherClass = Class.forName(otherClassStr);
                        result = otherClass.isAssignableFrom(current);
                    } catch (ClassNotFoundException e) {
                    }
                    if (result) {
                        break;
                    }
                }
            }
        }
        return result;
    }


    /**
     * 获取当前类的fields
     * @param currentClass
     * @return
     */
    public List<Field> getFields(Class currentClass) {
        return PojoUtils.getDeclaredFields(currentClass);
    }

    public Field getField(Class currentClass, String fieldName) throws NoSuchFieldException {
        return PojoUtils.getDeclaredField(currentClass, fieldName);
    }

    //获取该注解对象的属性值
    public static Object getAnnotationValue(Annotation annotation, String property) {
        return AnnotationUtils.getAnnotationValue(annotation, property);
    }

    public <T extends Annotation> T getAnnotation(Class classzz, Class<T> annotationClass) {
        return AnnotationUtils.getDeclaredAnnotation(classzz, annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        return AnnotationUtils.getDeclaredAnnotation(field, annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        return AnnotationUtils.getDeclaredAnnotation(method, annotationClass);
    }

    public List<Annotation> getAnnotations(Object object) {
        return AnnotationUtils.getDeclaredAnnotations(object);
    }

    public Object getPropertyValue(Field field, Object obj) {
        Object result;
        Method readMethod = PojoUtils.getReadMethod(field);
        if (readMethod != null) {
            result = PojoUtils.getMethodValue(readMethod, obj);
        } else result = PojoUtils.getFieldValue(field, obj);
        return result;
    }

    public Object getPropertyValue(Object obj, String property) {
        if (obj == null) {
            throw new IllegalArgumentException("rdt get object property value error, cause by object is null");
        }
        try {
            if (obj instanceof Map) return ((Map) obj).get(property);
            return getPropertyValue(getField(obj.getClass(), property), obj);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setPropertyValue(Field field, Object obj, Object value) {
        Method writeMethod = PojoUtils.getWriteMethod(field);
        if (writeMethod != null) PojoUtils.setMethodValue(writeMethod, obj, value);
        else PojoUtils.setFieldValue(field, obj, value);
    }

    public void setPropertyValue(Object obj, String property, Object value) {
        try {
            if (obj instanceof Map) ((Map) obj).put(property, value);
            else setPropertyValue(getField(obj.getClass(), property), obj, value);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }


    /**
     * 转换字符串为指定类型值
     * 非基本类型时为null字符串时直接返回null
     * @param value
     * @param valType
     * @param whenEnumNotMatchError
     * @return
     */
    public <T> T castValue(String value, Class<T> valType, String whenEnumNotMatchError) {
        if ("null".equals(value)) {
            if (!valType.isPrimitive()) {
                return null;
            } else {
                value = null;
            }
        }
        Object result = null;
        if (valType.isEnum()) {
            if (value != null) {
                //解析枚举对应的值
                String currentName;
                if (value.matches("\\d+")) {
                    currentName = "ordinal";
                } else {
                    currentName = "name";
                }
                boolean hasMatch = false;
                for (Object constant : valType.getEnumConstants()) {
                    if (value.equals(getPropertyValue(constant, currentName).toString())) {
                        result = constant;
                        hasMatch = true;
                        break;
                    }
                }
                if (!hasMatch) {
                    if (whenEnumNotMatchError != null) {
                        whenEnumNotMatchError += value;
                    } else {
                        whenEnumNotMatchError = "value " + value + " cast to " + valType.getName() + " error";
                    }

                    throw new IllegalArgumentException(whenEnumNotMatchError);
                }
            }
        } else {
            result = cast(value, valType);
        }
        return (T) result;
    }


    /**
     * 解析注解string数组的值(String类型时字符串null将会被转换成null)
     * @param values
     * @param valType
     * @param whenEnumNotMatchError
     * @param <T>
     * @return
     */
    public <T> List<T> parseAnnotationValues(String[] values, Class<T> valType, String whenEnumNotMatchError) {
        List<T> results = new ArrayList<T>();
        if (values != null) {
            for (String value : values) {
                results.add(castValue(value, valType, whenEnumNotMatchError));
            }
        }
        return results;
    }

    public <T> List<T> parseAnnotationValues(String[] values, Class<T> valType) {
        return parseAnnotationValues(values, valType, null);
    }

    /**
     * 类型转换
     * @param obj
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T cast(Object obj, Class<T> clazz) {
        return TypeUtils.cast(obj, clazz);
    }

    public <T> T deepClone(T entity) {
        return Prototype.of(entity).deepClone().getModel();
    }

    /**
     * 转换成json
     * @param o
     * @return
     */
    public abstract String toJson(Object o);



    public String getConditionMark(Collection<String> keys, List<Object> values) {
        StringBuilder sb = new StringBuilder("[");
        if (keys != null && values != null) {
            int index = 0;
            for (String key : keys) {
                sb.append(key + "=" + values.get(index ++) + "&");
            }
            int length = sb.length();
            sb.delete(length - 1, length);
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * 返回new list
     * @param sourceList
     * @param otherList
     * @param isRemove
     * @param <T>
     * @return
     */
    public final <T> List<T> getNewList(List<T> sourceList, List<T> otherList, boolean isRemove) {
        List<T> result = getNewList(sourceList);
        if (isRemove && otherList != null && !otherList.isEmpty()) {
            result.removeAll(otherList);
        }
        return result;
    }

    public final <T> List<T> getNewList(List<T> sourceList) {
        return new ArrayList<T>(sourceList);
    }



    /**
     * 获取提示信息(可重写支持国际化)
     * @param tips
     * @return
     */
    public String getTipsContent(String tips) {
        return StringUtils.isNotEmpty(tips) ? tips : null;
    }


    public final String getRedText(String text) {
        if (text == null) {
            text = "";
        }
        String result = "\033[1;31m" + text +  "\033[0m";
        return result;
    }

    public final String getYellowText(String text) {
        if (text == null) {
            text = "";
        }
        String result = "\033[0;33;1m" + text +  "\033[0m";
        return result;
    }

    /**
     * 输出关系log
     * @param relyDescribe
     * @param show
     */
    public void revealModifyDescribeLogs(ModifyDescribe relyDescribe, boolean show) {
        if (relyDescribe != null) {

            List<Object> dataList = new ArrayList<Object>();
            
            StringBuilder sb = new StringBuilder("{}");

            dataList.add(relyDescribe.getEntityClass().getName());
            sb.append("\nModifyDescribe:");
            
            resolveDescribeLogsKeyAndValue(sb, "\n\t", dataList, Arrays.asList("target class", "index")
            , Arrays.asList(new Object[] {relyDescribe.getTargetClass().getName(), relyDescribe.getIndex()}));

            boolean hasWarn = getResolveDescribeLogsModifyConditionsAndModifyColumnsHasWarn(sb, dataList, relyDescribe);
            revealDescribeLogs(sb.toString(), hasWarn, show, dataList.toArray());

        }

    }

    public void revealModifyRelyDescribeLogs(ModifyRelyDescribe relyDescribe, boolean show) {
        if (relyDescribe != null) {
            List<Object> dataList = new ArrayList<Object>();
            StringBuilder sb = new StringBuilder("{}");
            dataList.add(relyDescribe.getEntityClass().getName());
            sb.append("\nModifyRelyDescribe:");
            resolveDescribeLogsKeyAndValue(sb, "\n\t", dataList,
                    Arrays.asList(
                            "target class", "rely column", "group",
                            "index","valType", "valList",
                            "updateIgnoresValList", "notInValList"
                    ),
                    Arrays.asList(
                            new Object[] {
                                    relyDescribe.getTargetClass().getName(),
                                    relyDescribe.getRelyColumn().getProperty(),
                                    relyDescribe.getGroup(),
                                    relyDescribe.getIndex(),
                                    relyDescribe.getValType().getName(),
                                    relyDescribe.getValList(),
                                    relyDescribe.getUpdateIgnoresValList(),
                                    relyDescribe.getNotInValList()
                            }
                    )
            );

            boolean hasWarn = getResolveDescribeLogsModifyConditionsAndModifyColumnsHasWarn(sb, dataList, relyDescribe);
            revealDescribeLogs(sb.toString(), hasWarn, show, dataList.toArray());
        }

    }


    protected boolean getResolveDescribeLogsModifyConditionsAndModifyColumnsHasWarn(StringBuilder logContentBuilder, List<Object> dataList, ModifyDescribe describe) {
        boolean warn = false;
        if (describe != null) {
            logContentBuilder.append("\n\tModifyConditions:");

            List<ModifyCondition> conditionList = describe.getConditionList();
            if (conditionList.isEmpty()) {
                warn = true;
                logContentBuilder.append("\n\t\t" + getRedText("【has no condition, please make sure no problem.】"));
            } else {
                for (ModifyCondition modifyCondition : conditionList) {
                    logContentBuilder.append("\n\t\tModifyCondition:");

                    Column column = modifyCondition.getColumn();
                    Column targetColumn = modifyCondition.getTargetColumn();
                    String mark = column.getProperty() + (column.getIsTransient() ? getYellowText("[transient]") : "") +"(" + column.getPropertyClass().getName() +
                            ") ==> "+ targetColumn.getProperty()
                            + "(" + targetColumn.getPropertyClass().getName()   + ")";

                    resolveDescribeLogsKeyAndValue(logContentBuilder, "\n\t\t\t",
                            dataList,
                            Arrays.asList((String) null),
                            Arrays.asList(new Object[]{mark})
                    );
                }
            }

            logContentBuilder.append("\n\tModifyColumns:");
            List<ModifyColumn> columnList = describe.getColumnList();

            if (!columnList.isEmpty()) {
                for (ModifyColumn modifyColumn : columnList) {
                    logContentBuilder.append("\n\t\tModifyColumn:");

                    Column column = modifyColumn.getColumn();
                    Column targetColumn = modifyColumn.getTargetColumn();
                    String mark = column.getProperty() + (column.getIsTransient() ? getYellowText("[transient]") : "") +"(" + column.getPropertyClass().getName() +
                            ") ==> "+ targetColumn.getProperty()
                            + "(" + targetColumn.getPropertyClass().getName()   + ")";

                    if (!column.getPropertyClass().equals(targetColumn.getPropertyClass())) {
                        warn = true;
                        mark = mark + getRedText("【please make sure can cast.】");
                    }

                    resolveDescribeLogsKeyAndValue(logContentBuilder, "\n\t\t\t",
                            dataList,
                            Arrays.asList(null,  "fillShowType",
                                    "fillSaveType", "fillShowIgnoresType", "fillSaveIgnoresType"),
                            Arrays.asList(new Object[]{mark, modifyColumn.getFillShowType(),
                                    modifyColumn.getFillSaveType(), modifyColumn.getFillShowIgnoresType(), modifyColumn.getFillSaveIgnoresType()})
                    );
                }
            }
        }
        return warn;
    }



    protected void resolveDescribeLogsKeyAndValue(StringBuilder logContentBuilder, String connector, List<Object> dataList, Collection<String> keys, List<Object> values) {
        if (!keys.isEmpty()) {
            int index = 0;
            for (String key : keys) {
                Object value = values.get(index ++);
                if (value != null) {
                    if (value instanceof List && ((List) value).isEmpty()) {
                        continue;
                    }

                    if (connector != null) {
                        logContentBuilder.append(connector);
                    }

                    if (key != null) {
                        //key存在时
                        logContentBuilder.append(key);
                        logContentBuilder.append(": ");
                    }
                    //拼接{},用于显示对应值
                    logContentBuilder.append("{}");
                    dataList.add(value);
                }
            }
        }
    }


    /**
     * 当处于debug及warn级别时均显示,show控制info级别是否显示
     * @param text e.g:  content: {}, date: {}
     * @param hasWarn
     * @param show
     * @param data
     */
    public void revealDescribeLogs(String text, boolean hasWarn, boolean show, Object... data) {
        if (hasWarn) {
            if (logger.isWarnEnabled()) {
                logger.warn(text, data);
            } else {
                System.err.println(String.format(text.replace("{}", "%s"), data));
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(text, data);
            } else {
                if (show) {
                    if (logger.isInfoEnabled()) {
                        logger.info(text, data);
                    } else {
                        System.out.println(String.format(text.replace("{}", "%s"), data));
                    }
                }
            }

        }
    }

    public RdtFillType getFillShowType(ClassModel classModel, Column column, RdtFillType fillShowType) {
        if (fillShowType == null || RdtFillType.DEFAULT == fillShowType) {
            if (classModel.getIsVoClass()) {
                return RdtFillType.ENABLE;
            }
        }
        return fillShowType;
    }

    public RdtFillType getFillSaveType(ClassModel classModel, Column column, RdtFillType fillShowType) {
        if (fillShowType == null || RdtFillType.DEFAULT == fillShowType) {
            if (classModel.getIsVoClass()) {
                return RdtFillType.ENABLE;
            }
        }
        return fillShowType;
    }


    public static <T> List<T> split(String text, String symbol, Class<T> type, boolean containsNullOrBlank) {
        List<T> results = new ArrayList<T>();
        type = type == null ? (Class<T>)String.class : type;
        String[] splitArray = null;
        if (text != null) {
            splitArray = text.split(symbol, -1);
        }
        if (splitArray != null) {
            for (String value : splitArray) {
                if (type == String.class) {
                    boolean add = containsNullOrBlank || StringUtils.isNotBlank(value);
                    if (add) {
                        results.add((T)value);
                    }
                } else {
                    T castValue = cast(value, type);
                    if (containsNullOrBlank || castValue != null) {
                        results.add(castValue);
                    }
                }
            }
        }
        return results;
    }

    public static List<String> split(String text, String symbol, boolean containsNullOrBlank) {
        return split(text, symbol, String.class, containsNullOrBlank);
    }

    public static List<String> split(String text, String symbol) {
        return split(text, symbol, false);
    }

    public static <T> List<T> split(String text, String symbol, Class<T> type) {
        return split(text, symbol, type, false);
    }

    public static String join(Iterable<?> iterable, String separator, boolean isToEmpty) {
        if (separator == null) {
            separator = ",";
        }
        StringBuilder builder = new StringBuilder();
        if (iterable != null) {
            Iterator iterator = iterable.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append(separator);
            }
            int length = builder.length();
            if (length > 0) {
                builder.delete(length - separator.length(), length);
            }
        }
        String result = builder.toString();
        return StringUtils.isNotEmpty(result) ? result : isToEmpty ? "" : null;
    }

    public static String join(Iterable<?> iterable, String separator) {
        return join(iterable, separator, false);
    }

    public static Class getFieldClass(Field field) {
        return PojoUtils.getFieldClass(field);
    }

    public static <T> T[] newInstanceArray(Class<T> classType, int length) {
        return PojoUtils.newInstanceArray(classType, length);
    }
}
