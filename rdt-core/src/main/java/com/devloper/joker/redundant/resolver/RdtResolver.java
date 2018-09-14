package com.devloper.joker.redundant.resolver;

import com.devloper.joker.redundant.annotation.base.RdtBaseEntity;
import com.devloper.joker.redundant.annotation.base.RdtBaseField;
import com.devloper.joker.redundant.model.ClassModel;
import com.devloper.joker.redundant.model.Column;
import com.devloper.joker.redundant.utils.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class RdtResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

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
        return PackageClassUtils.getClasses(packageName);
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
        if (!one) {
            if (type.isArray()) {
                type = type.getComponentType();
            } else if (ClassUtils.familyClass(type, Collection.class)) {
                if (ClassUtils.familyClass(type, Map.class)) throw new IllegalArgumentException("the relation many type not support map");
                if (ClassUtils.getActualTypeArgumentsLength(field.getGenericType()) == 0) {
                    throw new IllegalArgumentException("the relation many type not has actual class type");
                }
                type = ClassUtils.getActualTypeArgumentClass(field, 0);
            }
        }
        if (type.isArray()) throw new IllegalArgumentException("the relation one/many actual class type must be not array");
        else if (ClassUtils.familyClass(type, Collection.class)) throw new IllegalArgumentException("the relation one/many actual class type must be not collection");
        return type;
    }


    /**
     * 验证当前column与target column的格式,比较条件时类型必须一致
     * @param column
     * @param targetColumn
     * @param classModel
     * @param targetClassModel
     * @param condition 是否为条件field
     */
    public void columnCompareVerification(Column column, Column targetColumn, ClassModel classModel, ClassModel targetClassModel, boolean condition) {
        Class currentEntityClass = PojoUtils.getFieldLocalityClass(classModel.getPropertyFieldMap().get(column.getProperty()));
        Class targetEntityClass = PojoUtils.getFieldLocalityClass(targetClassModel.getPropertyFieldMap().get(targetColumn.getProperty()));
            /*String hint = targetEntityClass.getName() + " property " + targetColumn.getProperty()
                    + " type is " + targetColumn.getPropertyClass().getName() + ", " + currentEntityClass.getName() + " property " + column.getProperty() + " type is " +
                    column.getPropertyClass().getName();*/

        String hint = "rdt column --- " + currentEntityClass.getName() + " property " + column.getProperty() + "(" + column.getPropertyClass().getName() +
                ") ==> [" +  targetEntityClass.getName() + " property " + targetColumn.getProperty()
                + "(" + targetColumn.getPropertyClass().getName()   + ")]";
        if (!column.getPropertyClass().equals(targetColumn.getPropertyClass())) {  //类型应一致
            if (condition) throw new IllegalArgumentException(hint);  //条件的类型必须一致
            else {
                logger.warn(hint + ", please make sure can cast.");
            }
        } else {
            logger.debug(hint);
        }
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
        } else  result = PojoUtils.getFieldValue(field, obj);
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
     * 类型转换
     * @param obj
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T cast(Object obj, Class<T> clazz) {
        return TypeUtils.cast(obj, clazz);
    }

    /**
     * 转换成json
     * @param o
     * @return
     */
    public abstract String toJson(Object o);
}
