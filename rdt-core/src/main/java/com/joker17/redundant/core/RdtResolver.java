package com.joker17.redundant.core;

import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.annotation.RdtId;
import com.joker17.redundant.annotation.base.RdtBaseEntity;
import com.joker17.redundant.annotation.base.RdtBaseField;
import com.joker17.redundant.model.*;
import com.joker17.redundant.model.commons.ClassTypeEnum;
import com.joker17.redundant.support.Prototype;
import com.joker17.redundant.utils.*;
import com.joker17.redundant.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public abstract class RdtResolver {

    protected final Logger logger = LoggerFactory.getLogger(RdtResolver.class);

    private List<Class> baseEntityAnnotationClassList = null;
    private List<Class> primaryIdAnnotationClassList = null;
    private List<Class> columnTransientAnnotationClassList = null;

    private RdtProperties properties;

    protected Map<Class, List<List<ComplexModel>>> complexResultListMap = new HashMap<Class, List<List<ComplexModel>>>(16);

    /**
     * 当前complexClass所对应的解析结果
     */
    protected Map<Class, List<ComplexAnalysis>> complexAnalysisResultListMap = new HashMap<Class, List<ComplexAnalysis>>(16);

    void setRdtProperties(RdtProperties properties) {
        this.properties = properties;
    }

    /**
     * 哪些作为修改基本类的注解
     *
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
     *
     * @return
     */
    protected abstract Class<?>[] customBaseEntityAnnotations();


    public List<Class> getClasses(String packageName) {
        return PackageClassUtils.getClassList(packageName);
    }

    /**
     * 当前类是否为修改基本类
     *
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
     *
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
     *
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
     *
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
     *
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
                if (ClassUtils.familyClass(type, Map.class))
                    throw new IllegalArgumentException(hint + "the relation many type not support map");
                if (ClassUtils.getActualTypeArgumentsLength(field.getGenericType()) == 0) {
                    throw new IllegalArgumentException(hint + "the relation many type not has actual class type");
                }
                type = ClassUtils.getActualTypeArgumentClass(field, 0);
            }
        }
        if (type.isArray())
            throw new IllegalArgumentException(hint + "the relation " + (one ? "one" : "many") + " actual class type must be not array");
        else if (ClassUtils.familyClass(type, Collection.class))
            throw new IllegalArgumentException(hint + "the relation " + (one ? "one" : "many") + " actual class type must be not collection");
        return type;
    }


    /**
     * 验证当前column与target column的格式,比较条件时类型必须一致
     *
     * @param column
     * @param targetColumn
     * @param classModel
     * @param targetClassModel
     * @param condition                  是否为条件field
     * @param isTargetColumnNotTransient
     */
    public void columnCompareVerification(Column column, Column targetColumn, ClassModel classModel, ClassModel targetClassModel, boolean condition, boolean isTargetColumnNotTransient, boolean modifyColumnMustSameType) {
        Class currentEntityClass = column.getEntityClass();
        Class targetEntityClass = targetColumn.getEntityClass();
            /*String hint = targetEntityClass.getName() + " property " + targetColumn.getProperty()
                    + " type is " + targetColumn.getPropertyClass().getName() + ", " + currentEntityClass.getName() + " property " + column.getProperty() + " type is " +
                    column.getPropertyClass().getName();*/

        String hint = "rdt column --- " + currentEntityClass.getName() + " property " + column.getProperty() + "(" + column.getPropertyClass().getName() +
                ") ==> [" + targetEntityClass.getName() + " property " + targetColumn.getProperty()
                + "(" + targetColumn.getPropertyClass().getName() + ")]";

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
        return new String[]{"java.time.LocalDateTime"};
    }


    /**
     * 获取class是否为基础类型
     *
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

    public ClassModel getClassModel(Class entityClass) {
        return properties.getClassModel(entityClass);
    }



    public Class getTypeClass(ParameterizedType parameterizedType, int index) {
        if (parameterizedType != null) {
            Type[] types = parameterizedType.getActualTypeArguments();
            int typeLength = types == null ? 0 : types.length;
            if (index >= typeLength) {
                throw new IllegalArgumentException(String.format("index out of bounds, actual type arguments length %s, cause by index %s", typeLength, index));
            }
            Type resultType = types[index];
            if (resultType instanceof Class) {
                return (Class) resultType;
            }
        }
        return null;
    }

    public Type getGenericType(Method method, int parameterIndex) {
        if (method == null) {
            return null;
        }
        Type[] types = method.getGenericParameterTypes();
        int typeLength = types == null ? 0 : types.length;

        if (parameterIndex >= typeLength) {
            throw new IllegalArgumentException(String.format("%s  parameters index out of bounds, actual parameters length %s, cause by parameter index %s", method.toGenericString(), typeLength, parameterIndex));
        }
        return types[parameterIndex];
    }

    public Type getGenericType(Field field) {
        if (field != null) {
            return field.getGenericType();
        }
        return null;
    }


    public Class getGenericActualTypeClass(Method method, int parameterIndex, int index) {
        return getGenericActualTypeClass(getGenericType(method, parameterIndex), index);
    }

    public Class getGenericActualTypeClass(Type type, int index) {
        if (type == null) {
            return null;
        } else if (type instanceof Class) {
            Class typeClass = (Class) type;
            return typeClass.isArray() ? typeClass.getComponentType() : typeClass;
        } else if (type instanceof ParameterizedType) {
            return getTypeClass((ParameterizedType) type, index);
        } else if (type instanceof GenericArrayType) {
            return getGenericActualTypeClass(((GenericArrayType) type).getGenericComponentType(), index);
        }
        return null;
    }

    public Class getGenericActualTypeClass(Field field, int index) {
        if (field != null) {
            return getGenericActualTypeClass(field.getGenericType(), index);
        }
        return null;
    }


    /**
     * 获取泛型对象的实际类型
     * @param genericBean
     * @param position
     * @return
     */
    public Class getGenericActualTypeClass(Object genericBean, int position) {
        return getGenericActualTypeClass(genericBean, null, position);
    }

    public Class getGenericActualTypeClass(Object genericBean, Class parentType, int position) {
        if (genericBean == null) {
            return null;
        }
        return getGenericActualTypeClass(genericBean.getClass(), parentType, position);
    }

    /**
     * 获取class对应索引位置的泛型实际类型
     * @param leafClass
     * @param position
     * @return
     */
    public Class getGenericActualTypeClass(Class leafClass, int position) {
        return getGenericActualTypeClass(leafClass, null, position);
    }

    public Class getGenericActualTypeClass(Class leafClass, Class parentType, int position) {
        if (leafClass != null) {
            Type[] typeParameters = leafClass.getTypeParameters();
            int typeParametersLength = typeParameters == null ? 0 : typeParameters.length;
            if (typeParametersLength > 0) {
                //存在定义泛型时
                return null;
            }
            Type superclassType = leafClass.getGenericSuperclass();
            if (superclassType == null || (superclassType != null && superclassType == Object.class)) {
                Type[] genericInterfaces = leafClass.getGenericInterfaces();
                if (genericInterfaces != null && genericInterfaces.length == 1) {
                    superclassType = genericInterfaces[0];
                }
            }
            ParameterizedType parameterizedType = null;
            while(superclassType != null && parameterizedType == null) {
                if (superclassType instanceof Class) {
                    superclassType = ((Class)superclassType).getGenericSuperclass();
                } else if (superclassType instanceof ParameterizedType) {
                    parameterizedType = (ParameterizedType)superclassType;
                    superclassType = null;
                }

                if (parameterizedType != null) {
                    Type rawType = parameterizedType.getRawType();
                    boolean clear = false;
                    if (rawType != null && rawType instanceof Class) {
                        if (parentType != null && !parentType.isAssignableFrom((Class) rawType)) {
                            clear = true;
                        }
                    }
                    if (clear) {
                        parameterizedType = null;
                    }
                }
            }
            if (parameterizedType != null) {
                return getTypeClass(parameterizedType, position);
            }
        }
        return null;
    }


    /**
     * 获取当前类的fields
     *
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
    public Object getAnnotationValue(Annotation annotation, String property) {
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
     *
     * @param value
     * @param valType
     * @param errorPrefix
     * @return
     */
    public <T> T castValue(String value, Class<T> valType, String errorPrefix) {
        if ("null".equals(value)) {
            if (!valType.isPrimitive()) {
                return null;
            } else {
                value = null;
            }
        }
        errorPrefix = errorPrefix == null ? "" : errorPrefix.endsWith(" ") ? errorPrefix : errorPrefix + " ";
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
                    String enumCastError = "the enum " + valType.getName() + " type has no val: " + value;
                    throw new IllegalArgumentException(errorPrefix + enumCastError);
                }
            }
        } else {
            try {
                result = cast(value, valType);
            } catch (Exception e) {
                String castError = "the value " + value + " can not cast to : " + valType.getName();
                throw new IllegalArgumentException(errorPrefix + castError);
            }
        }
        return (T) result;
    }


    /**
     * 解析注解string数组的值(String类型时字符串null将会被转换成null)
     *
     * @param values
     * @param valType
     * @param errorPrefix
     * @param <T>
     * @return
     */
    public <T> List<T> parseAnnotationValues(String[] values, Class<T> valType, String errorPrefix) {
        List<T> results = new ArrayList<T>();
        if (values != null) {
            for (String value : values) {
                results.add(castValue(value, valType, errorPrefix));
            }
        }
        return results;
    }

    public <T> List<T> parseAnnotationValues(String[] values, Class<T> valType) {
        return parseAnnotationValues(values, valType, null);
    }

    /**
     * 类型转换
     *
     * @param obj
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T cast(Object obj, Class<T> clazz) {
        return TypeUtils.cast(obj, clazz);
    }

    public <T> T deepClone(T entity) {
        return Prototype.of(entity).deepClone().getModel();
    }

    /**
     * 转换成json
     *
     * @param o
     * @return
     */
    public abstract String toJson(Object o);


    public String getConditionMark(Collection<String> keys, List<Object> values) {
        StringBuilder sb = new StringBuilder("[");
        if (keys != null && values != null) {
            int index = 0;
            for (String key : keys) {
                sb.append(key).append("=").append(values.get(index++)).append("&");
            }
            int length = sb.length();
            sb.delete(length - 1, length);
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * 返回new list
     *
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

    public <T> List<T> getNotNullList(List<T> dataList) {
        return dataList == null ? Collections.<T>emptyList() : dataList;
    }


    /**
     * 获取提示信息(可重写支持国际化)
     *
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
        String result = "\033[1;31m" + text + "\033[0m";
        return result;
    }

    public final String getYellowText(String text) {
        if (text == null) {
            text = "";
        }
        String result = "\033[0;33;1m" + text + "\033[0m";
        return result;
    }

    public final String getBrownText(String text) {
        if (text == null) {
            text = "";
        }
        String result = "\033[0;33;3m" + text + "\033[0m";
        return result;
    }

    /**
     * 输出关系log
     *
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
                    , Arrays.asList(new Object[]{relyDescribe.getTargetClass().getName(), relyDescribe.getIndex()}));

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
                            "index", "valType", "valList",
                            "updateIgnoresValList", "notInValList"
                    ),
                    Arrays.asList(
                            new Object[]{
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

    public void revealModifyGroupDescribeLogs(ModifyGroupDescribe groupDescribe, boolean show) {
        if (groupDescribe != null) {
            List<Object> dataList = new ArrayList<Object>();
            StringBuilder sb = new StringBuilder("{}");
            dataList.add(groupDescribe.getEntityClass().getName());
            sb.append("\nModifyGroupDescribe:");
            resolveDescribeLogsKeyAndValue(sb, "\n\t", dataList,
                    Arrays.asList(
                            "target class", "index"
                    ),
                    Arrays.asList(
                            new Object[]{
                                    groupDescribe.getTargetClass().getName(),
                                    groupDescribe.getIndex()
                            }
                    )
            );

            boolean hasWarn = getResolveDescribeLogsModifyGroupKeysColumnAndModifyGroupConcatColumnsHasWarn(sb, dataList, groupDescribe);
            revealDescribeLogs(sb.toString(), hasWarn, show, dataList.toArray());
        }

    }


    protected String getDescribeColumnWithTargetColumnMark(Column column, Column targetColumn) {
        StringBuilder builder = new StringBuilder(column.getProperty());
        builder.append((column.getIsTransient() ? getYellowText("[transient]") : ""));
        builder.append("(");
        builder.append(getFormatClassName(column.getPropertyClass()));
        builder.append(") ==> ");
        builder.append(targetColumn.getProperty());
        builder.append("(");
        builder.append(getFormatClassName(targetColumn.getPropertyClass()));
        builder.append(")");
        return builder.toString();
    }

    protected String getDescribeColumnWithTargetColumnMarks(List<Column> columnList, List<Column> targetColumnList) {
        StringBuilder builder = new StringBuilder();

        if (columnList != null && targetColumnList != null) {
            int size = columnList.size();
            boolean one = size == 1;
            if (!one) {
                builder.append("[");
            }
            String connector = ", ";
            for (int i = 0; i < size; i++) {
                builder.append(getDescribeColumnWithTargetColumnMark(columnList.get(i), targetColumnList.get(i)));
                builder.append(connector);
            }
            int builderLength = builder.length();
            builder.delete(builderLength - connector.length(), builderLength);
            if (!one) {
                builder.append("]");
            }
        }
        return builder.toString();
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
                    String mark = getDescribeColumnWithTargetColumnMark(column, targetColumn);
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
                    String mark = getDescribeColumnWithTargetColumnMark(column, targetColumn);
                    if (!column.getPropertyClass().equals(targetColumn.getPropertyClass())) {
                        warn = true;
                        mark = mark + getRedText("【please make sure can cast.】");
                    }

                    resolveDescribeLogsKeyAndValue(logContentBuilder, "\n\t\t\t",
                            dataList,
                            Arrays.asList(null, "fillShowType",
                                    "fillSaveType", "fillShowIgnoresType", "fillSaveIgnoresType"),
                            Arrays.asList(new Object[]{mark, modifyColumn.getFillShowType(),
                                    modifyColumn.getFillSaveType(), modifyColumn.getFillShowIgnoresType(), modifyColumn.getFillSaveIgnoresType()})
                    );
                }
            }
        }
        return warn;
    }

    /**
     * 检测值是否可以进行转换
     *
     * @param groupBaseColumn
     * @return
     */
    protected boolean checkGroupColumnCanCast(ModifyGroupBaseColumn groupBaseColumn) {
        Class columnBasicClass = groupBaseColumn.getColumnBasicClass();
        Class targetColumnClass = groupBaseColumn.getTargetColumnClass();
        return columnBasicClass == String.class || columnBasicClass == targetColumnClass;
    }

    protected boolean getResolveDescribeLogsModifyGroupKeysColumnAndModifyGroupConcatColumnsHasWarn(StringBuilder logContentBuilder, List<Object> dataList, ModifyGroupDescribe describe) {
        boolean warn = false;
        if (describe != null) {
            logContentBuilder.append("\n\tModifyGroupKeysColumn:");
            ModifyGroupKeysColumn modifyGroupKeysColumn = describe.getModifyGroupKeysColumn();
            {
                Column column = modifyGroupKeysColumn.getColumn();
                Column targetColumn = modifyGroupKeysColumn.getTargetColumn();
                String mark = getDescribeColumnWithTargetColumnMark(column, targetColumn);
                if (!checkGroupColumnCanCast(modifyGroupKeysColumn)) {
                    warn = true;
                    mark = mark + getRedText("【please make sure can cast.】");
                }

                List<String> currentKeysList = new ArrayList<String>(Arrays.asList(
                        null, "connector", "columnBasicClass",
                        "columnClassType"
                ));

                List<Object> currentValuesList = new ArrayList<Object>(Arrays.asList(new Object[]{
                        mark, modifyGroupKeysColumn.getConnector(), getFormatClassName(modifyGroupKeysColumn.getColumnBasicClass()),
                        modifyGroupKeysColumn.getColumnClassType()
                }));

                Class gainClass = modifyGroupKeysColumn.getGainClass();

                if (gainClass != null) {
                    currentKeysList.addAll(Arrays.asList("gainClass", "gainSelectColumn", "gainConditionMark"));
                    currentValuesList.addAll(Arrays.asList(
                            getFormatClassName(gainClass),
                            modifyGroupKeysColumn.getGainSelectColumn().getProperty(),
                            getDescribeColumnWithTargetColumnMarks(modifyGroupKeysColumn.getGainConditionColumnList(), modifyGroupKeysColumn.getGainConditionValueRelyColumnList())
                            )
                    );
                }

                resolveDescribeLogsKeyAndValue(logContentBuilder, "\n\t\t",
                        dataList,
                        currentKeysList,
                        currentValuesList
                );
            }

            logContentBuilder.append("\n\tModifyGroupConcatColumns:");
            List<ModifyGroupConcatColumn> modifyGroupConcatColumnList = describe.getModifyGroupConcatColumnList();

            if (!modifyGroupConcatColumnList.isEmpty()) {
                for (ModifyGroupConcatColumn concatColumn : modifyGroupConcatColumnList) {
                    logContentBuilder.append("\n\t\tModifyGroupConcatColumn:");

                    Column column = concatColumn.getColumn();
                    Column targetColumn = concatColumn.getTargetColumn();
                    String mark = getDescribeColumnWithTargetColumnMark(column, targetColumn);
                    if (!checkGroupColumnCanCast(concatColumn)) {
                        warn = true;
                        mark = mark + getRedText("【please make sure can cast.】");
                    }

                    resolveDescribeLogsKeyAndValue(logContentBuilder, "\n\t\t\t",
                            dataList,
                            Arrays.asList(
                                    null, "connector", "columnBasicClass",
                                    "columnClassType", "isStartBasicConnector", "isBasicNotConnectorOptFirst",
                                    "fillShowType", "fillSaveType"
                            ),
                            Arrays.asList(new Object[]{
                                    mark, concatColumn.getConnector(), getFormatClassName(concatColumn.getColumnBasicClass()),
                                    concatColumn.getColumnClassType(), concatColumn.isStartBasicConnector(), concatColumn.isBasicNotConnectorOptFirst(),
                                    concatColumn.getFillShowType(), concatColumn.getFillSaveType()
                            })
                    );
                }
            } else {
                warn = true;
                logContentBuilder.append("\n\t\t" + getBrownText("【has no concat columns, please make sure no problem.】"));
            }
        }
        return warn;
    }


    protected void resolveDescribeLogsKeyAndValue(StringBuilder logContentBuilder, String connector, List<Object> dataList, Collection<String> keys, List<Object> values) {
        if (!keys.isEmpty()) {
            int index = 0;
            for (String key : keys) {
                Object value = values.get(index++);
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
     *
     * @param text    e.g:  content: {}, date: {}
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


    public <T> List<T> split(String text, String symbol, Class<T> type, boolean containsNullOrBlank) {
        List<T> results = new ArrayList<T>();
        type = type == null ? (Class<T>) String.class : type;
        String[] splitArray = null;
        if (text != null) {
            splitArray = text.split(symbol, -1);
        }
        if (splitArray != null) {
            for (String value : splitArray) {
                if (type == String.class) {
                    boolean add = containsNullOrBlank || StringUtils.isNotBlank(value);
                    if (add) {
                        results.add((T) value);
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

    public List<String> split(String text, String symbol, boolean containsNullOrBlank) {
        return split(text, symbol, String.class, containsNullOrBlank);
    }

    public List<String> split(String text, String symbol) {
        return split(text, symbol, false);
    }

    public <T> List<T> split(String text, String symbol, Class<T> type) {
        return split(text, symbol, type, false);
    }

    public String join(Iterable<?> iterable, String separator, boolean isToEmpty) {
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

    public String join(Iterable<?> iterable, String separator) {
        return join(iterable, separator, false);
    }

    public Class getFieldClass(Field field) {
        return PojoUtils.getFieldClass(field);
    }

    public <T> T[] newInstanceArray(Class<T> classType, int length) {
        return PojoUtils.newInstanceArray(classType, length);
    }

    public <T> T newInstance(Class<T> classType) {
        if (classType == null) {
            throw new IllegalArgumentException("classType must not be null");
        }
        if (classType.isInterface() && (Map.class == classType || Map.class.isAssignableFrom(classType))) {
            return (T) new HashMap();
        }
        try {
            return classType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("can't new instance class %s", classType.getName()), e);
        }
    }

    /**
     * 获取格式化后的class名称
     *
     * @param type
     * @return char[].class ==> char[]
     */
    public String getFormatClassName(Class type) {
        String className = type.getName();
        if (className.contains("[")) {
            //是数组时获取个数
            int count = appearNumber(className, "[");
            String simpleType = "";
            if (!className.endsWith(";")) {
                String temp = className.substring(count);

                Map<Class, String> basicTypeMap = new HashMap<Class, String>();
                basicTypeMap.put(int.class, "I");
                basicTypeMap.put(long.class, "J");
                basicTypeMap.put(short.class, "S");
                basicTypeMap.put(boolean.class, "Z");
                basicTypeMap.put(float.class, "F");
                basicTypeMap.put(double.class, "D");
                basicTypeMap.put(byte.class, "B");
                basicTypeMap.put(char.class, "C");

                for (Class key : basicTypeMap.keySet()) {
                    String val = basicTypeMap.get(key);
                    if (val.equals(temp)) {
                        simpleType = key.getName();
                        break;
                    }
                }
            } else {
                simpleType = className.substring(count + 1, className.length() - 1);
            }

            StringBuilder builder = new StringBuilder(simpleType);
            for (int i = 0; i < count; i++) {
                builder.append("[]");
            }
            return builder.toString();
        }
        return className;
    }

    /**
     * 获取指定字符串出现的次数
     *
     * @param srcText
     * @param findText
     * @return
     */
    protected static int appearNumber(String srcText, String findText) {
        int count = 0;
        int index = 0;
        while ((index = srcText.indexOf(findText, index)) != -1) {
            index = index + findText.length();
            count++;
        }
        return count;
    }

    /**
     * 获取groupKey所对应列的数据值列表
     *
     * @param groupKeysValueList    与selectColumnValue类型一致的数据列表
     * @param modifyGroupKeysColumn
     * @param selectColumnValue
     * @return
     */
    public List<Object> getGroupKeysExpectValueList(List<Object> groupKeysValueList, ModifyGroupKeysColumn modifyGroupKeysColumn, Column selectColumnValue) {
        Class targetColumnClass = modifyGroupKeysColumn.getTargetColumnClass();
        List<Object> groupKeysExpectValueList;
        if (selectColumnValue.getPropertyClass() == targetColumnClass) {
            groupKeysExpectValueList = groupKeysValueList;
        } else {
            groupKeysExpectValueList = getCastTypeList(groupKeysValueList, targetColumnClass);
        }
        return groupKeysExpectValueList;
    }

    /**
     * 获取当前列实际类型的数据值列表
     *
     * @param groupKeysValueList    与selectColumnValue类型一致的数据列表
     * @param modifyGroupKeysColumn
     * @param selectColumnValue
     * @return
     */
    public List<Object> getColumnPropertyValueList(List<Object> groupKeysValueList, ModifyGroupKeysColumn modifyGroupKeysColumn, Column selectColumnValue) {
        Class columnBasicClass = modifyGroupKeysColumn.getColumnBasicClass();
        List<Object> columnPropertyValueList;

        if (selectColumnValue.getPropertyClass() == columnBasicClass) {
            columnPropertyValueList = groupKeysValueList;
        } else {
            columnPropertyValueList = getCastTypeList(groupKeysValueList, columnBasicClass);
        }
        return columnPropertyValueList;
    }

    /**
     * 获取groupKeyValue所对应项的所有值列表
     *
     * @param groupKeyValue         groupKey列的属性值
     * @param modifyGroupKeysColumn
     * @return
     */
    public List<Object> getGroupKeysExpectValueList(Object groupKeyValue, ModifyGroupKeysColumn modifyGroupKeysColumn) {

        Class columnBasicClass = modifyGroupKeysColumn.getColumnBasicClass();
        Class targetColumnClass = modifyGroupKeysColumn.getTargetColumnClass();
        String connector = modifyGroupKeysColumn.getConnector();
        boolean isBasicEqTargetColumnClass = columnBasicClass.equals(targetColumnClass);

        //获取当前key的所有值
        List<Object> groupKeysExpectValueList = null;

        if (groupKeyValue != null) {
            ClassTypeEnum columnClassType = modifyGroupKeysColumn.getColumnClassType();
            switch (columnClassType) {
                case BASIC:
                    if (columnBasicClass == String.class) {
                        groupKeysExpectValueList = split((String) groupKeyValue, connector, targetColumnClass);
                    } else {
                        groupKeysExpectValueList = Arrays.asList(isBasicEqTargetColumnClass ? groupKeyValue : cast(groupKeyValue, targetColumnClass));
                    }
                    break;
                case ARRAY:
                    groupKeysExpectValueList = new ArrayList<Object>(16);
                    for (Object val : (Object[]) groupKeyValue) {
                        groupKeysExpectValueList.add(isBasicEqTargetColumnClass ? val : cast(val, targetColumnClass));
                    }
                    break;
                case SET:
                case LIST:
                    groupKeysExpectValueList = new ArrayList<Object>(16);
                    for (Object val : (Collection) groupKeyValue) {
                        groupKeysExpectValueList.add(isBasicEqTargetColumnClass ? val : cast(val, targetColumnClass));
                    }
                    break;
            }
        }
        return groupKeysExpectValueList;
    }

    /**
     * 通过columnPropertyValueList转换为GroupColumn的属性值
     *
     * @param columnPropertyValueList columnPropertyValue与实际类型一致
     * @param groupColumn
     * @return
     */
    public Object getGroupColumnPropertyValue(List<Object> columnPropertyValueList, ModifyGroupBaseColumn groupColumn) {
        Class columnBasicClass = groupColumn.getColumnBasicClass();
        Class columnClass = groupColumn.getColumnClass();
        String connector = groupColumn.getConnector();

        //转换为对应属性值
        Object columnPropertyValue = null;
        ClassTypeEnum columnClassType = groupColumn.getColumnClassType();
        switch (columnClassType) {
            case BASIC:

                boolean isStartBasicConnector = true;
                boolean isBasicNotConnectorOptFirst = true;

                if (groupColumn instanceof ModifyGroupConcatColumn) {
                    ModifyGroupConcatColumn groupConcatColumn = (ModifyGroupConcatColumn) groupColumn;
                    isStartBasicConnector = groupConcatColumn.isStartBasicConnector();
                    isBasicNotConnectorOptFirst = groupConcatColumn.isBasicNotConnectorOptFirst();
                }

                boolean isConnector = isStartBasicConnector && columnBasicClass == String.class;
                if (isConnector) {
                    columnPropertyValue = join(columnPropertyValueList, connector);
                } else {
                    columnPropertyValue = columnPropertyValueList.get(isBasicNotConnectorOptFirst ? 0 : columnPropertyValueList.size() - 1);
                }
                break;
            case ARRAY:
                columnPropertyValue = columnPropertyValueList.toArray(newInstanceArray(columnBasicClass, columnPropertyValueList.size()));
                break;
            case SET:
            case LIST:
                Collection columnPropertyCollectionValue = null;
                if (columnClass == List.class || columnClass == ArrayList.class) {
                    columnPropertyCollectionValue = columnPropertyValueList;
                } else if (columnClass == Set.class || columnClass == LinkedHashSet.class) {
                    columnPropertyCollectionValue = new LinkedHashSet();
                    columnPropertyCollectionValue.addAll(columnPropertyValueList);
                } else {
                    try {
                        //其他类型通过newInstance进行实例化
                        columnPropertyCollectionValue = (Collection) columnClass.newInstance();
                        columnPropertyCollectionValue.addAll(columnPropertyValueList);
                    } catch (Exception e) {
                    }
                }
                columnPropertyValue = columnPropertyCollectionValue;
                break;
        }
        return columnPropertyValue;
    }

    public List<Object> getCastTypeList(Collection<Object> source, Class typeClass) {
        List<Object> resultList = new ArrayList<Object>(16);
        if (source != null) {
            for (Object current : source) {
                resultList.add(cast(current, typeClass));
            }
        }
        return resultList;
    }

    /**
     * 通过column集合获取对应的property列表数据
     *
     * @param columnCollection
     * @return
     */
    public List<String> getPropertyList(Collection<Column> columnCollection) {
        List<String> propertyList = new ArrayList<String>(16);
        if (columnCollection != null) {
            for (Column column : columnCollection) {
                propertyList.add(column.getProperty());
            }
        }
        return propertyList;
    }

    /**
     * 检查关系(complexClass中不应该包含引用它的对象类以及相关被引用的对象类)
     *
     * @param complexClass
     */
    protected void checkComplexClassRelation(Class complexClass) {
        ClassModel complexClassClassModel = getClassModel(complexClass);
        List<ComplexModel> complexModelList = complexClassClassModel.getComplexModelList();
        if (!complexModelList.isEmpty()) {
            //存在关联对象关系时
            for (ComplexModel complexModel : complexModelList) {
                checkComplexClassRelation(complexModel, complexClassClassModel);
            }
        }
    }

   /**
     * 检查当前complexModel是否满足关系
     *
     * @param complexModel
     * @param complexClassClassModel
     */
    protected void checkComplexClassRelation(ComplexModel complexModel, ClassModel complexClassClassModel) {
        //验证关联对象的类型是否不存在于当前父引用类及父引用类的父引用类中

        Class complexModelCurrentType = complexModel.getCurrentType();
        Class complexModelOwnerType = complexModel.getOwnerType();

        if (complexModelCurrentType == complexModelOwnerType) {
            //自己本身时
            return;
        }
        for (Class parentContainsClass : complexClassClassModel.getParentContainsClassSet()) {
            if (parentContainsClass == complexModelOwnerType) {
                //父类与complexModel所在的类一致时跳过
                continue;
            }

            if (complexModelCurrentType == parentContainsClass) {
                //complexModel所存在的属性类型与其所对应的父类属性一致时(提示非法)
                String text = String.format("%s not allowed property %s, cause by: the property type %s equals parent relation type", complexModelOwnerType.getName(), complexModel.getProperty(), complexModelCurrentType.getName());
                throw new IllegalArgumentException(text);
            }
            List<String> parentContainsClassNameList = new ArrayList<String>(16);
            parentContainsClassNameList.add(parentContainsClass.getName());

            //检查complexModel与父引用类的父引用类中的关系
            checkComplexClassRelation(complexModel, getClassModel(parentContainsClass));
        }
    }


    /**
     * 解析复杂model之间的关系
     */
    protected void parseComplexModel() {
        Map<Class, List<ComplexModel>> classComplexModelsMap = properties.getClassComplexModelsMap();
        for (Class complexClass : classComplexModelsMap.keySet()) {
            //验证关系,当存在问题时直接抛出异常
            checkComplexClassRelation(complexClass);
            getComplexAnalysisList(complexClass);
        }
    }


    /**
     * 获取当前复杂对象的组合,返回数组按照倒序的属性方式
     *
     * @param complexClass
     * @return
     */
    protected List<List<ComplexModel>> getComplexModelParseResult(Class complexClass) {
        List<List<ComplexModel>> results = complexResultListMap.get(complexClass);
        if (results == null) {
            synchronized (this) {
                results = complexResultListMap.get(complexClass);
                boolean flag = results == null;
                if (flag) {
                    results = new ArrayList<List<ComplexModel>>();
                    complexResultListMap.put(complexClass, results);
                    Map<Class, List<ComplexModel>> classComplexModelsMap = properties.getClassComplexModelsMap();
                    List<ComplexModel> complexObjects = classComplexModelsMap.get(complexClass);
                    if (complexObjects != null) {
                        for (ComplexModel complexObject : complexObjects) {
                            if (complexObject.getOwnerBase()) {
                                List<ComplexModel> result = new ArrayList<ComplexModel>();
                                result.add(complexObject);
                                results.add(result);
                            } else {
                                List<List<ComplexModel>> currents = getComplexModelParseResult(complexObject.getOwnerType());
                                for (List<ComplexModel> current : currents) {
                                    List<ComplexModel> result = new ArrayList<ComplexModel>();
                                    result.add(complexObject);
                                    result.addAll(current);
                                    results.add(result);
                                }
                            }
                        }
                    }

                }
            }
        }
        return results;
    }

    /**
     * 获取当前非base类所对应的所有关联关系数据集合
     *
     * @param complexClass
     * @return
     */
    public List<ComplexAnalysis> getComplexAnalysisList(Class complexClass) {
        List<ComplexAnalysis> result = complexAnalysisResultListMap.get(complexClass);
        if (result == null) {
            synchronized (this) {
                result = complexAnalysisResultListMap.get(complexClass);
                if (result == null) {
                    result = new ArrayList<ComplexAnalysis>(16);
                    complexAnalysisResultListMap.put(complexClass, result);
                    List<List<ComplexModel>> complexResults = getComplexModelParseResult(complexClass);
                    for (List<ComplexModel> complexResult : complexResults) {
                        ComplexAnalysis complexAnalysis = getComplexAnalysis(complexResult);
                        result.add(complexAnalysis);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 解析当前关系列表的结果,即base类的关联关系
     *
     * @param complexModelList
     * @return e.g
     * {"currentTypeList": ["com.joker17.rdt_sbm.model.Reply"],"hasMany":false,"oneList":[true],"prefix":"reply","propertyList":["reply"],"rootClass":"com.joker17.rdt_sbm.domain.Article"}
     */
    protected ComplexAnalysis getComplexAnalysis(List<ComplexModel> complexModelList) {
        ComplexAnalysis complexAnalysis = new ComplexAnalysis();
        StringBuilder sb = new StringBuilder();
        int size = complexModelList.size();
        for (int i = size - 1; i >= 0; i--) {
            ComplexModel complexModel = complexModelList.get(i);
            if (i == size - 1) {
                complexAnalysis.setRootClass(complexModel.getOwnerType());
                complexAnalysis.setHasMany(false);
            }
            List<Boolean> oneList = complexAnalysis.getOneList();
            List<String> propertyList = complexAnalysis.getPropertyList();
            List<Class> currentTypeList = complexAnalysis.getCurrentTypeList();

            String property = complexModel.getProperty();
            Boolean isOne = complexModel.getIsOne();
            if (!isOne) complexAnalysis.setHasMany(true);
            sb.append(property);
            sb.append(".");

            currentTypeList.add(complexModel.getCurrentType());
            propertyList.add(property);
            oneList.add(isOne);
        }

        int length = sb.length();
        if (length > 0) {
            sb.delete(length - 1, length);
            complexAnalysis.setPrefix(sb.toString());
        }
        return complexAnalysis;
    }





}
