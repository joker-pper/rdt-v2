package com.devloper.joker.redundant.utils;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;


public class PojoUtils {

    /**
     * 将copyEntity指定属性赋值给entity
     *
     * @param entity
     * @param copyEntity
     * @param properties
     * @param <T>
     */
    public static <T> void copyProperties(T entity, T copyEntity, String[] properties) {
        if (entity != null && copyEntity != null) {
            if (properties != null) {
                /*Class<T> entityClass = (Class<T>) entity.getClass();
                for (String property: properties) {
                    setFieldValue(entityClass, property, entity, getFieldValue(entityClass, copyEntity, property));
                }*/
                copyPropertiesForObject(entity, copyEntity, properties);
            }
        }
    }

    /**
     * 将copyEntity指定属性赋值给entity,前提是指定的属性要存在,且属性类型一致
     *
     * @param entity
     * @param copyEntity
     * @param properties -map或者其他对象
     */
    public static void copyPropertiesForObject(Object entity, Object copyEntity, String[] properties) {

        if (entity != null && copyEntity != null) {

            Class entityClass = entity.getClass();
            Class copyEntityClass = copyEntity.getClass();
            if (entity instanceof Map) {
                throw new IllegalArgumentException("entity class can't be map instance");
            }
            boolean flag = (copyEntity instanceof Map);

            for (String property : properties) {
                Object result;
                if (flag) {
                    result = ((Map) copyEntity).get(property);
                } else {
                    result = getFieldValue(copyEntityClass, copyEntity, property);
                }
                setFieldValue(entityClass, property, entity, result);
            }
        }


    }


    /**
     * 将copyEntity的属性值全部复制给entity
     *
     * @param entity
     * @param copyEntity
     * @param <T>
     */
    public static <T> void copyAllProperties(T entity, T copyEntity) {
        if (entity != null && copyEntity != null) {
            Class<T> entityClass = (Class<T>) entity.getClass();
            Field[] fields = entityClass.getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    setFieldValue(field, entity, getFieldValue(field, copyEntity));
                   /* String property = field.getName();
                    setFieldValue(entityClass, property, entity, getFieldValue(entityClass, copyEntity, property));
                */
                }
            }
        }
    }

    /**
     * 将copyEntity的除了指定属性列表的属性值直接赋值给entity的对应属性
     *
     * @param entity
     * @param copyEntity
     * @param besidesProperties 除了指定的属性列表
     * @param <T>
     */
    public static <T> void copyBesidesProperties(T entity, T copyEntity, String[] besidesProperties) {
        if (entity != null && copyEntity != null) {
            Class<T> entityClass = (Class<T>) entity.getClass();
            Field[] fields = entityClass.getDeclaredFields();
            if (fields != null) {
                Map<String, Object> besidesPropertiesMap = null;
                if (besidesProperties != null) {
                    besidesPropertiesMap = new HashMap<String, Object>();
                    for (String property : besidesProperties) {
                        besidesPropertiesMap.put(property, true);
                    }
                }
                for (Field field : fields) {
                    boolean copy = true;
                    if (besidesPropertiesMap != null) {
                        if (besidesPropertiesMap.containsKey(field.getName())) {
                            copy = false;
                        }
                    }
                    if (copy) {
                        setFieldValue(field, entity, getFieldValue(field, copyEntity));
                    }
                }
            }
        }
    }


    /**
     * 只将copyEntity指定属性中不为null的赋值给entity
     *
     * @param entity
     * @param copyEntity
     * @param properties
     * @param <T>
     */
    public static <T> void copyNotNullProperties(T entity, T copyEntity, String[] properties) {
        copyNotNullPropertiesForObject(entity, copyEntity, properties);
    }

    /**
     * 只将copyEntity指定属性中不为null的赋值给entity,前提是指定的属性要存在,且属性类型一致
     *
     * @param entity
     * @param copyEntity
     * @param properties -map或者其他对象
     * @param <T>
     */
    public static <T> void copyNotNullPropertiesForObject(Object entity, Object copyEntity, String[] properties) {
        if (entity != null && copyEntity != null) {

            Class entityClass = entity.getClass();
            Class copyEntityClass = copyEntity.getClass();
            if (entity instanceof Map) {
                throw new IllegalArgumentException("entity class can't be map instance");
            }
            boolean flag = (copyEntity instanceof Map);

            for (String property : properties) {
                Object result;
                if (flag) {
                    result = ((Map) copyEntity).get(property);
                } else {
                    result = getFieldValue(copyEntityClass, copyEntity, property);
                }
                if (result != null) {
                    setFieldValue(entityClass, property, entity, result);
                }
            }
        }
    }


    /**
     * 将copyEntity的除了指定属性列表的属性中的属性,将值不为null的赋值给entity对应的属性
     *
     * @param entity
     * @param copyEntity
     * @param besidesProperties 除了指定的属性列表
     * @param <T>
     */
    public static <T> void copyBesidesPropertiesWithNotNull(T entity, T copyEntity, String[] besidesProperties) {
        if (entity != null && copyEntity != null) {
            Class<T> entityClass = (Class<T>) entity.getClass();
            Field[] fields = entityClass.getDeclaredFields();
            if (fields != null) {
                Map<String, Object> besidesPropertiesMap = null;
                if (besidesProperties != null) {
                    besidesPropertiesMap = new HashMap<String, Object>();
                    for (String property : besidesProperties) {
                        besidesPropertiesMap.put(property, true);
                    }
                }
                for (Field field : fields) {
                    boolean copy = true;
                    if (besidesPropertiesMap != null) {
                        if (besidesPropertiesMap.containsKey(field.getName())) {  //排除该属性的复制
                            copy = false;
                        }
                    }
                    if (copy) {
                        Object result = getFieldValue(field, copyEntity);
                        if (result != null) {
                            setFieldValue(field, entity, result);
                        }
                    }
                }
            }
        }
    }

    /**
     * 将copyEntity的除了指定属性列表的属性中的属性,将值不为null(且类型为String时通过!isEmpty()验证)的赋值给entity对应的属性
     *
     * @param entity
     * @param copyEntity
     * @param besidesProperties 除了指定的属性列表
     * @param <T>
     */
    public static <T> void copyBesidesPropertiesWithNotNullAndNotEmpty(T entity, T copyEntity, String[] besidesProperties) {
        if (entity != null && copyEntity != null) {
            Class<T> entityClass = (Class<T>) entity.getClass();
            Field[] fields = entityClass.getDeclaredFields();
            if (fields != null) {
                Map<String, Object> besidesPropertiesMap = null;
                if (besidesProperties != null) {
                    besidesPropertiesMap = new HashMap<String, Object>();
                    for (String property : besidesProperties) {
                        besidesPropertiesMap.put(property, true);
                    }
                }
                for (Field field : fields) {
                    boolean copy = true;
                    if (besidesPropertiesMap != null) {
                        if (besidesPropertiesMap.containsKey(field.getName())) {  //排除该属性的复制
                            copy = false;
                        }
                    }
                    if (copy) {
                        Object result = getFieldValue(field, copyEntity);
                        if (result != null) {
                            boolean flag = true;
                            if (result instanceof String) {
                                flag = !((String) result).isEmpty();  //判断为字符时是否不为空
                            }
                            if (flag) {
                                setFieldValue(field, entity, result);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 将copyEntity的除了指定属性列表的属性中的属性,将值不为null(且类型为String时通过trim()和!isEmpty()验证,值将变成trim()后的值)的赋值给entity对应的属性
     *
     * @param entity
     * @param copyEntity
     * @param besidesProperties 除了指定的属性列表
     * @param <T>
     */
    public static <T> void copyBesidesPropertiesWithNotNullAndTrimNotEmpty(T entity, T copyEntity, String[] besidesProperties) {
        if (entity != null && copyEntity != null) {
            Class<T> entityClass = (Class<T>) entity.getClass();
            Field[] fields = entityClass.getDeclaredFields();
            if (fields != null) {
                Map<String, Object> besidesPropertiesMap = null;
                if (besidesProperties != null) {
                    besidesPropertiesMap = new HashMap<String, Object>();
                    for (String property : besidesProperties) {
                        besidesPropertiesMap.put(property, true);
                    }
                }
                for (Field field : fields) {
                    boolean copy = true;
                    if (besidesPropertiesMap != null) {
                        if (besidesPropertiesMap.containsKey(field.getName())) {  //排除该属性的复制
                            copy = false;
                        }
                    }
                    if (copy) {
                        Object result = getFieldValue(field, copyEntity);
                        if (result != null) {
                            boolean flag = true;
                            if (result instanceof String) {
                                result = ((String) result).trim();
                                flag = !((String) result).isEmpty();  //判断为字符时是否不为空
                            }
                            if (flag) {
                                setFieldValue(field, entity, result);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 只将copyEntity指定属性中不为null(且类型为String时通过!isEmpty()验证)的赋值给entity
     *
     * @param entity
     * @param copyEntity
     * @param properties
     * @param <T>
     */
    public static <T> void copyNotNullAndNotEmptyProperties(T entity, T copyEntity, String[] properties) {
        copyNotNullAndNotEmptyPropertiesForObject(entity, copyEntity, properties);
    }

    /**
     * 只将copyEntity指定属性中不为null(且类型为String时通过!isEmpty()验证)的赋值给entity
     * 前提是指定的属性要存在,且属性类型一致
     *
     * @param entity
     * @param copyEntity -map或者其他对象
     * @param properties
     */
    public static void copyNotNullAndNotEmptyPropertiesForObject(Object entity, Object copyEntity, String[] properties) {
        if (entity != null && copyEntity != null) {

            Class entityClass = entity.getClass();
            Class copyEntityClass = copyEntity.getClass();
            if (entity instanceof Map) {
                throw new IllegalArgumentException("entity class can't be map instance");
            }
            boolean flag = (copyEntity instanceof Map);

            for (String property : properties) {
                Object result;
                if (flag) {
                    result = ((Map) copyEntity).get(property);
                } else {
                    result = getFieldValue(copyEntityClass, copyEntity, property);
                }

                if (result != null) {
                    flag = true;
                    if (result instanceof String) {
                        flag = !((String) result).isEmpty();
                    }
                    if (flag) {
                        setFieldValue(entityClass, property, entity, result);
                    }
                }

            }
        }
    }


    /**
     * 只将copyEntity指定属性中不为null(且类型为String时通过trim()和!isEmpty()验证,值将变成trim()后的值)的赋值给entity
     *
     * @param entity
     * @param copyEntity
     * @param properties
     * @param <T>
     */
    public static <T> void copyNotNullAndTrimNotEmptyProperties(T entity, T copyEntity, String[] properties) {
        copyNotNullAndTrimNotEmptyPropertiesForObject(entity, copyEntity, properties);
    }

    /**
     * 只将copyEntity指定属性中不为null(且类型为String时通过trim()和!isEmpty()验证,值将变成trim()后的值)的赋值给entity
     * 前提是指定的属性要存在,且属性类型一致
     *
     * @param entity
     * @param copyEntity -map或者其他对象
     * @param properties
     */
    public static void copyNotNullAndTrimNotEmptyPropertiesForObject(Object entity, Object copyEntity, String[] properties) {
        if (entity != null && copyEntity != null) {

            Class entityClass = entity.getClass();
            Class copyEntityClass = copyEntity.getClass();
            if (entity instanceof Map) {
                throw new IllegalArgumentException("entity class can't be map instance");
            }
            boolean flag = (copyEntity instanceof Map);

            for (String property : properties) {
                Object result;
                if (flag) {
                    result = ((Map) copyEntity).get(property);
                } else {
                    result = getFieldValue(copyEntityClass, copyEntity, property);
                }

                if (result != null) {
                    flag = true;
                    if (result instanceof String) {
                        result = ((String) result).trim();
                        flag = !((String) result).isEmpty();
                    }
                    if (flag) {
                        setFieldValue(entityClass, property, entity, result);
                    }
                }

            }
        }
    }

    public static List<Field> getDeclaredFields(Class classzz) {
        List<Field> fieldList = new ArrayList<Field>();
        if (classzz != null) {
            List<String> propertys = new ArrayList<String>();
            for (; classzz != Object.class; classzz = classzz.getSuperclass()) {
                for (Field field : getActualDeclaredFields(classzz)) {
                    String fieldName = field.getName();
                    if (propertys.contains(fieldName)) continue;
                    propertys.add(fieldName);
                    fieldList.add(field);
                }
            }

        }
        return fieldList;
    }

    public static List<Field> getActualDeclaredFields(Class classzz) {
        List<Field> fieldList = new ArrayList<Field>();
        if (classzz != null) {
            Field[] fields = classzz.getDeclaredFields();
            if (fields != null) fieldList.addAll(Arrays.asList(fields));
        }
        return fieldList;
    }


    /**
     * 循环向上转型, 获取对象的 DeclaredField
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     * @return 父类中的属性对象
     */
    public static Field getDeclaredField(Object object, String fieldName) throws NoSuchFieldException {
        Class<?> clazz = null;
        if (object != null) {
            clazz = object.getClass();
        }
        return getDeclaredField(clazz, fieldName);
    }

    public static Field getDeclaredField(Class classzz, String fieldName) throws NoSuchFieldException {
        Field field = null;
        NoSuchFieldException exception = null;
        if (classzz != null && fieldName != null) {
            for(; classzz != Object.class; classzz = classzz.getSuperclass()) {
                try {
                    field = getActualDeclaredField(classzz, fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    exception = e;
                } catch (Exception e) {
                }
            }
            if (field == null) {
                if (exception == null) exception = new NoSuchFieldException(classzz.getName() + " has no field " + fieldName);
                throw exception;
            }
        }
        return field;
    }


    public static Field getActualDeclaredField(Class classzz, String fieldName) throws NoSuchFieldException {
        Field field = null;
        if (classzz != null) {
            try {
                field = classzz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {

            } catch (SecurityException e) {
            }
            if (field == null) throw new NoSuchFieldException(classzz.getName() + " has no field " + fieldName);
        }
        return field;
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     * @param object : 子类对象
     * @param methodName : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @return 父类中的方法对象
     */
    public static Method getDeclaredMethod(Object object, String methodName, Class<?> ... parameterTypes) throws NoSuchMethodException {
        Method method = null;
        for(Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = getActualDeclaredMethod(clazz, methodName, parameterTypes);
                break;
            } catch (Exception e) {
            }
        }
        if (method == null) throw new NoSuchMethodException();
        return method;
    }

    public static Method getActualDeclaredMethod(Class<?> clazz, String methodName, Class<?> ... parameterTypes) throws NoSuchMethodException {
        Method method = null;
        if (clazz != null) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                throw e;
            } catch (SecurityException e) {
            }
        }
        if (method == null) throw new NoSuchMethodException();
        return method;
    }

    public static Object getFieldValue(Class<?> currClass, Object object, String property) {
        if (currClass != null && object != null && property != null) {
            try {
                Field f = getDeclaredField(object, property);
                return getFieldValue(f, object);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(currClass + " has no property: " + property);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static Object getFieldValue(Field field, Object object) {
        if (field != null && object != null) {
            try {
                field.setAccessible(true); //设置属性可以访问
                return field.get(object);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /***
     * 获取该对象的属性值
     * @param object
     * @param property
     * @param <T>
     * @return
     */
    public static <T> Object getFieldValue(T object, String property) {
        if (object != null && property != null) {
            Class<T> currClass = (Class<T>) object.getClass();
            return getFieldValue(currClass, object, property);
        }
        return null;
    }

    public static <T> Object getFieldResult(T object, String property) {
        Object result = null;
        try {
            result = getFieldValue(object, property);
        } catch (Exception e) {
        }
        return result;
    }

    public static Object getFieldResult(Field field, Object object) {
        Object result = null;
        try {
            result = getFieldValue(field, object);
        } catch (Exception e) {
        }
        return result;
    }


    /**
     * 通过field给当前对象的属性赋值
     * @param currClass
     * @param property --- 属性名称
     * @param object
     * @param value --- 属性值
     */
    public static void setFieldValue(Class<?> currClass, String property, Object object, Object value) {
        if (currClass != null && object != null && property != null) {
            try {
                Field f = getDeclaredField(object, property);
                setFieldValue(f, object, value);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(currClass + " has no property: " + property);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setFieldValue(Field field, Object object, Object value) {
        if (field != null && object != null) {
            try {
                field.setAccessible(true); //设置属性可以访问
                field.set(object, value);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static <T> void setFieldValue(T object, String property, Object value) {
        if (object != null && property != null) {
            Class<T> currClass = (Class<T>) object.getClass();
            setFieldValue(currClass, property, object, value);
        }
    }
    public static <T> void setFieldValueDirect(T object, String property, Object value) {
        try {
            setFieldValue(object, property, value);
        } catch (Exception e) {
        }
    }


    public static Object getStaticMethodResult(Class classzz, String methodName, Class[] paramTypes, Object... params) {
        return getMethodResult(classzz, methodName, null, paramTypes, params);
    }

    public static Object getMethodResult(Class classzz, String methodName, Object target, Class[] paramTypes, Object... params) {
        Object result = null;
        if (classzz != null) {
            Method method = null;
            try {
                method = getActualDeclaredMethod(classzz, methodName, paramTypes);
            } catch (NoSuchMethodException e) {
            }
            result = getMethodValue(method, target, params);
        }
        return result;
    }


    //获取方法执行后的返回值
    public static <T> Object getMethodValue(Method method, Object object, Object... args) {
        Object result = null;
        if (method != null) {
            method.setAccessible(true);
            try {
                result = method.invoke(object, args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static <T> Object setMethodValue(Method method, Object object, Object... args) {
        Object result = null;
        if (method != null) {
            method.setAccessible(true);
            try {
                result = method.invoke(object, args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    //获取该方法所在的类
    public static Class getMethodLocalityClass(Method method) {
        if (method != null) {
            return method.getDeclaringClass();
        }
        return null;
    }

    //获取该field所在的类
    public static Class getFieldLocalityClass(Field field) {
        if (field != null) {
            return field.getDeclaringClass();
        }
        return null;
    }

    //获取该field的类
    public static Class getFieldClass(Field field) {
        if (field != null) {
            return field.getType();
        }
        return null;
    }

    //获取该field对应的getter()方法
    public static Method getReadMethod(Field field) {
        if (field != null) {
            Class classzz = getFieldLocalityClass(field);
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), classzz);
                return pd.getReadMethod();
            } catch (IntrospectionException e) {
            }
        }
        return null;
    }

    //获取该field对应的setter()方法
    public static Method getWriteMethod(Field field) {
        if (field != null) {
            Class classzz = getFieldLocalityClass(field);
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), classzz);
                return pd.getWriteMethod();
            } catch (IntrospectionException e) {
            }
        }
        return null;
    }

    public static Class<?> getActualTypeArgumentClass(Field field, int index) {
        if (field != null) {
            return getActualTypeArgumentClass(field.getGenericType(), index);
        }
        return null;
    }

    public static Class<?> getActualTypeArgumentClass(Type type, int index) {
        if (type != null) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] types = parameterizedType.getActualTypeArguments();  //获取真实的类型参数的数组
                int length = types.length;

                if (index >= length) {
                    throw new IllegalArgumentException("index out of bounds, total length is" + length);
                }
                return (Class<?>) types[index];
            } else {
                if (index == 0) return (Class<?>) type;
            }
        }

        return null;
    }


    public static List<String> getPropertyNames(Class<?> type) {
        List<String> list = null;
        if (type == null) {
            throw new IllegalArgumentException("can't getPropertyNames for javabean, becasue the class type is null");
        } else {
            BeanInfo beanInfo = null; // 获取类属性
            try {
                beanInfo = Introspector.getBeanInfo(type);
            } catch (IntrospectionException e) {
            }
            if (beanInfo != null) {
                list = new ArrayList<String>();
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (int i = 0; i < propertyDescriptors.length; i++) {
                    PropertyDescriptor descriptor = propertyDescriptors[i];
                    String propertyName = descriptor.getName();
                    if (propertyName.equals("class")) {
                        continue;
                    }
                    list.add(propertyName);
                }
            }
        }
        return list;
    }


    public static Object getPropertyValue(Field field, Object obj) {
        Object result;
        Method readMethod = getReadMethod(field);
        if (readMethod != null) {
            result = getMethodValue(readMethod, obj);
        } else  result = getFieldValue(field, obj);
        return result;
    }

    public static Object getPropertyValue(Object obj, String property) {
        try {
            if (obj instanceof Map) return ((Map) obj).get(property);
            return getPropertyValue(getDeclaredField(obj.getClass(), property), obj);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static void setPropertyValue(Field field, Object obj, Object value) {
        Method writeMethod = getWriteMethod(field);
        if (writeMethod != null) setMethodValue(writeMethod, obj, value);
        else setFieldValue(field, obj, value);
    }

    public static void setPropertyValue(Object obj, String property, Object value) {
        try {
            if (obj instanceof Map) ((Map) obj).put(property, value);
            else setPropertyValue(getDeclaredField(obj.getClass(), property), obj, value);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
