package com.joker17.redundant.spring;

import com.joker17.redundant.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;

public class PojoUtils {

    public static Object getFieldValue(String fieldName, Object target) {
        Field field = getField(target, fieldName);
        return getFieldValue(field, target);
    }

    public static Object getFieldValue(Field field, Object target) {
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, target);
    }

    public static void setFieldValue(Object target, String fieldName, Object value) {
        Field field = getField(target, fieldName);
        setFieldValue(field, target, value);
    }

    public static void setFieldValue(Field field, Object target, Object value) {
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, target, value);
    }

    public static Field getField(Class classzz, String fieldName) {
        return ReflectionUtils.findField(classzz, fieldName);
    }

    public static Field getField(Object target, String fieldName) {
        return ReflectionUtils.findField(target.getClass(), fieldName);
    }


    public static Method findMethod(Object target, String name, Class[] paramTypes) {
        Class<?> clazz = target.getClass();
        return ReflectionUtils.findMethod(clazz, name, paramTypes);
    }

    public static Object invokeMethod(Object target, String name, Class[] paramTypes, Object... args) {
        Method method = findMethod(target, name, paramTypes);
        return invokeMethod(target, method, args);
    }

    public static Object invokeMethod(Object target, Method method, Object... args) {
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method, target, args);
    }

    /**
     * 获取该field所在的类
     */
    public static Class getBeanType(Field field) {
        if (field != null) {
            return field.getDeclaringClass();
        }
        return null;
    }

    public static Class getBeanType(Method method) {
        if (method != null) {
            return method.getDeclaringClass();
        }
        return null;
    }


    public static Class getFieldType(Field field) {
        if (field != null) {
            return field.getType();
        }
        return null;
    }

    public static PropertyDescriptor getPropertyDescriptor(Class beanClass, String name) {
        return getPropertyDescriptor(getField(beanClass, name), beanClass);
    }

    public static PropertyDescriptor getPropertyDescriptor(Field field) {
        return getPropertyDescriptor(field, null);
    }

    public static PropertyDescriptor getPropertyDescriptor(Field field, Class beanClass) {
        beanClass = beanClass == null ? getBeanType(field) : beanClass;
        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor(field.getName(), beanClass);
        } catch (IntrospectionException e) {
        }
        return pd;
    }


    public static Method getReadMethod(Class targetClass, String property) {
        Field field = getField(targetClass, property);
        if (field != null) {
            return getReadMethod(getPropertyDescriptor(field, targetClass));
        }
        return null;
    }


    public static Method getReadMethod(PropertyDescriptor pd) {
        if (pd != null) {
            return pd.getReadMethod();
        }
        return null;
    }

    public static Method getWriteMethod(Class targetClass, String property) {
        Field field = getField(targetClass, property);
        if (field != null) {
            return getWriteMethod(getPropertyDescriptor(field, targetClass));
        }
        return null;
    }


    public static Method getWriteMethod(PropertyDescriptor pd) {
        if (pd != null) {
            return pd.getWriteMethod();
        }
        return null;
    }

    public static void setPropertyValue(Object target, String property, Object value) {
        if (target instanceof Map) {
            ((Map) target).put(property, value);
            return;
        }
        Field field = getField(target, property);
        setPropertyValue(target, field, value);
    }

    public static void setPropertyValue(Object target, Field field, Object value) {
        if (field != null) {
            Method method = getWriteMethod(getPropertyDescriptor(field, target.getClass()));
            if (method != null) {
                invokeMethod(target, method, value);
            } else {
                setFieldValue(field, target, value);
            }
        }
    }

    /**
     * 获取对象的关联属性值
     *
     * @param target
     * @param property
     * @return
     */
    public static Object getAssociatePropertyValue(Object target, String property) {
        String[] result = property.split("\\.");
        if (target != null) {
            if (result.length == 1) {
                return getPropertyValue(target, property);
            } else {
                //关联对象的属性值
                for (String attr : result) {
                    target = getPropertyValue(target, attr);
                    if (target == null) {
                        break;
                    }
                }
                return target;
            }
        }
        return null;
    }

    public static Object getPropertyValue(Object target, String property) {
        Assert.isTrue(StringUtils.isNotBlank(property), "property must be not blank");
        if (target instanceof Map) {
            return ((Map) target).get(property);
        }

        Field field = getField(target, property);
        return getPropertyValue(target, field);
    }


    public static Object getPropertyValue(Object target, Field field) {
        if (field != null) {
            Method method = getReadMethod(getPropertyDescriptor(field, target.getClass()));
            if (method != null) {
                return invokeMethod(target, method);
            } else {
                return getFieldValue(field, target);
            }
        }
        return null;
    }


    public static List<Method> getAllDeclaredMethodList(Class<?> leafClass) {
        return Arrays.asList(getAllDeclaredMethods(leafClass));
    }

    public static Method[] getAllDeclaredMethods(Class<?> leafClass) {
        return ReflectionUtils.getAllDeclaredMethods(leafClass);
    }


    public static List<Field> getDeclaredFieldList(Class<?> leafClass) {
        final List<Field> fieldList = new ArrayList<Field>(32);
        ReflectionUtils.doWithFields(leafClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                fieldList.add(field);
            }
        });
        return fieldList;
    }

    public static Field[] getDeclaredFields(Class<?> leafClass) {
        List<Field> fieldList = getDeclaredFieldList(leafClass);
        return fieldList.toArray(new Field[fieldList.size()]);
    }

    public static <T> T instantiateClass(Class<T> clazz) {
        if (clazz.isInterface() && (Map.class == clazz || isAssignableFrom(clazz, Map.class))) {
            return (T) new HashMap();
        }
        return BeanUtils.instantiateClass(clazz);
    }


    public static String[] getProperties(Class<?> leafClass, boolean containsSelf, boolean containsStatic) {
        List<String> propertiesList = getPropertiesList(leafClass, containsSelf, containsStatic);
        return propertiesList.toArray(new String[propertiesList.size()]);
    }

    public static String[] getProperties(Class<?> leafClass) {
        return getProperties(leafClass, false, false);
    }

    public static List<String> getPropertiesList(Class<?> leafClass, boolean containsSelf, boolean containsStatic) {
        List<Field> fieldList = getDeclaredFieldList(leafClass);
        List<String> resultList = new ArrayList<String>();
        for (Field field : fieldList) {
            String property = field.getName();
            if (property.equals("this$0") && !containsSelf) {
                continue;
            }

            if (!containsStatic) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
            }

            resultList.add(property);
        }
        return resultList;
    }


    public static List<String> getPropertiesList(Class<?> leafClass) {
        return getPropertiesList(leafClass, false, false);
    }


    public static boolean isAssignableFrom(Class type, Class superType) {
        return superType.isAssignableFrom(type);
    }

    /**
     * 将source源的数据赋值到target中对应的属性中
     * @param source
     * @param target
     */
    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }


    public static void copyProperties(Object source, Object target, Class ignorePropertiesClass, String... ignoreProperties) {
        List<String> ignoreList = new ArrayList<String>(16);
        for (Field field : getDeclaredFieldList(ignorePropertiesClass)) {
            ignoreList.add(field.getName());
        }
        if (ignoreProperties != null) {
            for (String ignoreProperty : ignoreProperties) {
                ignoreList.add(ignoreProperty);
            }
        }
        copyProperties(source, target, ignoreList.toArray(new String[ignoreList.size()]));
    }

    /**
     * 获取Comparator对象
     *
     * @param sortProperty 排序字段
     * @param sort         asc/desc
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> getSortComparator(final String sortProperty, final String sort) {
        Comparator<T> comparator = new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                Object val1 = getPropertyValue(o1, sortProperty);
                Object val2 = getPropertyValue(o2, sortProperty);

                boolean val1NotNull = val1 != null;
                boolean val2NotNull = val2 != null;

                if (val1NotNull || val2NotNull) {
                    Class valClass = val1NotNull ? val1.getClass() : val2.getClass();

                    if (Comparable.class.isAssignableFrom(valClass)) {
                        boolean allNotNull = val1NotNull && val2NotNull;

                        boolean asc = sort.equals("asc");

                        if (allNotNull) {
                            if (asc) {
                                return ((Comparable) val1).compareTo(val2);
                            } else {
                                return ((Comparable) val2).compareTo(val1);
                            }
                        } else {
                            if (asc) {
                                return val1NotNull ? 1 : -1;
                            } else {
                                return val2NotNull ? 1 : -1;
                            }
                        }
                    }
                } else {
                    return 0;
                }
                return -1;
            }
        };
        return comparator;
    }


    public static boolean equals(Object first, Object second) {
        if (first == second) {
            return true;
        }
        if (first != null && second != null) {
            if (first instanceof Number || second instanceof Number) {
                //数值时比较文本值
                return first.toString().equals(second.toString());
            }
            return first.equals(second);
        }
        return false;
    }


    public static boolean equals(Number first, Number second) {
        if (first == second) {
            return true;
        }
        if (first != null && second != null) {
            return first.toString().equals(second.toString());
        }
        return false;
    }

    public static boolean contains(Object value, Collection<? extends Object> values) {
        if (values != null) {
            for (Object it : values) {
                if (equals(value, it)) {
                    return true;
                }
            }
        }
        return false;
    }

}



