package com.devloper.joker.redundant.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class AnnotationUtils {

    //获取该注解对象的属性值
    public static Object getAnnotationValue(Annotation annotation, String property) {
        Map map = getAnnotationValue(annotation);
        if (map != null) return map.get(property);
        return null;
    }

    public static Map getAnnotationValue(Annotation annotation) {
        Map<String, Object> map = getAnnotation(annotation);
        if (map != null) return (Map) map.get("memberValues");
        return null;
    }

    public static Class getAnnotationType(Annotation annotation) {
        if (annotation != null) return annotation.annotationType();
        return null;
    }

    public static Map<String, Object> getAnnotation(Annotation annotation) {
        if (annotation != null) {
            InvocationHandler invo = Proxy.getInvocationHandler(annotation); //获取被代理的对象
            Map<String, Object> result = new LinkedHashMap<String, Object>(16);
            result.put("type", annotation.annotationType());
            result.put("memberValues", PojoUtils.getFieldValue(invo, "memberValues"));
            return result;
        }
        return null;
    }

    public static <T> T getAnnotation(Annotation annotation, Class<T> annotationClass) {
        T result = null;
        if (annotation != null) {
            result = (T) annotation;
        }
        return result;
    }


    public static <T extends Annotation> T getDeclaredAnnotation(Class classzz, Class<T> annotationClass) {
        return getDeclaredAnnotation((Object) classzz, annotationClass);
    }

    public static <T extends Annotation> T getDeclaredAnnotation(Field field, Class<T> annotationClass) {
        return getDeclaredAnnotation((Object) field, annotationClass);
    }

    public static <T extends Annotation> T getDeclaredAnnotation(Method method, Class<T> annotationClass) {
        return getDeclaredAnnotation((Object) method, annotationClass);
    }

    private static <T extends Annotation> T getDeclaredAnnotation(Object object, Class<T> annotationClass) {
        T result = null;
        if (object != null && annotationClass != null) {
            result = getActualDeclaredAnnotation(object, annotationClass);
            if (result == null) {
                if (object instanceof Method) {
                    Method method = (Method) object;
                    Class<?> superClass = PojoUtils.getMethodLocalityClass(method).getSuperclass();
                    if (superClass != Object.class) {
                        try {
                            Method parentMethod = PojoUtils.getDeclaredMethod(superClass, method.getName(), method.getParameterTypes());
                            return getDeclaredAnnotation(parentMethod, annotationClass);
                        } catch (NoSuchMethodException e) {
                        }
                    }
                } else if (object instanceof Class) {
                    Class currentClass = (Class) object;
                    Class<?> superClass = currentClass.getSuperclass();
                    if (superClass != Object.class)
                        return getDeclaredAnnotation(superClass, annotationClass);
                } else if (object instanceof Field) {
                    Field field = (Field) object;
                    Class<?> superClass = PojoUtils.getFieldLocalityClass(field).getSuperclass();
                    if (superClass != Object.class) {
                        try {
                            Field parentField = PojoUtils.getDeclaredField(superClass, field.getName());
                            return getDeclaredAnnotation(parentField, annotationClass);
                        } catch (NoSuchFieldException e) {
                        }
                    }
                }
            }
        }
        return result;
    }


    public static <T extends Annotation> T getActualDeclaredAnnotation(Class classzz, Class<T> annotationClass) {
        return getActualDeclaredAnnotation((Object) classzz, annotationClass);
    }

    public static <T extends Annotation> T getActualDeclaredAnnotation(Field field, Class<T> annotationClass) {
        return getActualDeclaredAnnotation((Object) field, annotationClass);
    }

    public static <T extends Annotation> T getActualDeclaredAnnotation(Method method, Class<T> annotationClass) {
        return getActualDeclaredAnnotation((Object) method, annotationClass);
    }

    /**
     * 获取当前对象实际的注解对象
     *
     * @param object          class|method|field
     * @param annotationClass
     * @param <T>
     * @return
     */
    private static <T extends Annotation> T getActualDeclaredAnnotation(Object object, Class<T> annotationClass) {
        if (object != null && annotationClass != null) {
            Method method = getDeclaredMethod(object, "getDeclaredAnnotation", Class.class);
            if (method != null) {
                return (T) PojoUtils.getMethodValue(method, object, annotationClass);
            } else {
                method = getDeclaredMethod(object, "getDeclaredAnnotations");
                Annotation[] annotations = (Annotation[]) PojoUtils.getMethodValue(method, object);
                if (annotations != null && annotations.length > 0) {
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType().equals(annotationClass)) {
                            return (T) annotation;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<Annotation> getActualDeclaredAnnotations(Object object) {
        List<Annotation> result = new ArrayList<Annotation>();
        if (object != null) {
            Annotation[] annotations = null;
            if (object instanceof Method) {
                annotations = ((Method) object).getDeclaredAnnotations();
            } else if (object instanceof Field) {
                annotations = ((Field) object).getDeclaredAnnotations();
            } else if (object instanceof Class) {
                annotations = ((Class) object).getDeclaredAnnotations();
            }
            if (annotations != null)
                result.addAll(Arrays.asList(annotations));
        }
        return result;
    }


    public static List<Annotation> getDeclaredAnnotations(Object object) {
        List<Annotation> result = new ArrayList<Annotation>();
        if (object != null) {
            result.addAll(getActualDeclaredAnnotations(object));
            List<Class> resultClassList = new ArrayList<Class>();
            for (Annotation annotation : result) {
                resultClassList.add(annotation.annotationType());
            }

            if (object instanceof Method) {
                Method method = (Method) object;
                Class<?> superClass = PojoUtils.getMethodLocalityClass(method).getSuperclass();
                for (Class<?> clazz = superClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
                    try {
                        Method parentMethod = PojoUtils.getDeclaredMethod(superClass, method.getName(), method.getParameterTypes());
                        List<Annotation> parentResult = getActualDeclaredAnnotations(parentMethod);
                        for (Annotation annotation : parentResult) {
                            if (resultClassList.contains(annotation.annotationType())) continue;
                            else {
                                result.add(annotation);
                                resultClassList.add(annotation.annotationType());
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            } else if (object instanceof Class) {
                Class currentClass = (Class) object;
                Class<?> superClass = currentClass.getSuperclass();
                for (Class<?> clazz = superClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
                    try {
                        List<Annotation> parentResult = getActualDeclaredAnnotations(clazz);
                        for (Annotation annotation : parentResult) {
                            if (resultClassList.contains(annotation.annotationType())) continue;
                            else {
                                result.add(annotation);
                                resultClassList.add(annotation.annotationType());
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            } else if (object instanceof Field) {
                Field field = (Field) object;
                Class<?> superClass = PojoUtils.getFieldLocalityClass(field).getSuperclass();
                for (Class<?> clazz = superClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
                    try {
                        Field parentField = PojoUtils.getDeclaredField(clazz, field.getName());
                        List<Annotation> parentResult = getActualDeclaredAnnotations(parentField);
                        for (Annotation annotation : parentResult) {
                            if (resultClassList.contains(annotation.annotationType())) continue;
                            else {
                                result.add(annotation);
                                resultClassList.add(annotation.annotationType());
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        return result;
    }


    public static <T> Method getDeclaredMethod(T object, String methodName, Class<?>... parameterTypes) {
        try {
            return PojoUtils.getDeclaredMethod(object, methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
        }
        return null;
    }
}
