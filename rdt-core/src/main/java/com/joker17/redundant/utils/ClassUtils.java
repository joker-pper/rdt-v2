package com.joker17.redundant.utils;


import java.lang.reflect.*;


public class ClassUtils {
    /**
     * 通过反射执行srcClass的方法
     * @param object   --- the object the underlying method is invoked from
     * @param srcClass  ---要执行的类class
     * @param paramClasses  --- 参数class数组
     * @param methodName  ---方法名称
     * @param args   ---the arguments used for the method call
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object executeObjectMethod(Object object, Class<?> srcClass, Class<?>[] paramClasses, String methodName,  Object... args) throws SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = srcClass.getMethod(methodName, paramClasses);
        return method.invoke(object, args);
    }

    /**
     * 通过class的构造方法进行实例化
     * @param classzz  --- 要实例化的类
     * @param paramClasses  --- 参数类型集合
     * @param args  --- 参数值
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T newInstance(Class<T> classzz, Class<?>[] paramClasses, Object... args) throws NoSuchMethodException, InstantiationException,IllegalAccessException,InvocationTargetException {
        Constructor<T> constructor = classzz.getConstructor(paramClasses);
        return constructor.newInstance(args);
    }

    /**
     * 通过class直接进行实例化
     * @param classzz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T newInstance(Class<T> classzz) throws Exception {
        return classzz.newInstance();
    }

    /**
     * 通过classname进行实例化
     * @param classname
     * @return
     * @throws Exception
     */
    public static Object newInstance(String classname) throws Exception {
        return Class.forName(classname).newInstance();
    }

    /**
     * 通过反射获取当前class泛型的真实类型
     * e.g UserDao extends BaseDao<User> BaseDao构造方法获取泛型 getActualTypeArgumentClass(getClass(), 0);
     * @param currentClass --- class
     * @param index --- 索引
     * @return
     */
    public static Class<?> getActualTypeArgumentClass(Class<?> currentClass, int index) {
        Type type = currentClass.getGenericSuperclass();  //获取泛型父类
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] types = parameterizedType.getActualTypeArguments();  //获取真实的类型参数的数组
            return (Class<?>)types[index];
        }
        return null;
    }

    //获取当前field的真实类型
    public static Class<?> getActualTypeArgumentClass(Field field) {
        return getActualTypeArgumentClass(field, 0);
    }


    public static Class<?> getActualTypeArgumentClass(Field field, int index) {
        if (field != null) {
            return getActualTypeArgumentClass(field.getGenericType(), index);
        }
        return null;
    }

    public static int getActualTypeArgumentsLength(Type type) {
        if (type != null) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] types = parameterizedType.getActualTypeArguments();  //获取真实的类型参数的数组
                int length = types.length;
                return length;
            }
        }
        return 0;
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
                return (Class<?>)types[index];
            } else {
                if (index == 0) return (Class<?>) type;
            }
        }

        return null;
    }

    //是否属于家族中的类
    public static boolean familyClass(Class c1, Class c2) {
        if (c1 != null && c2 != null) {
            if (c1.equals(c2)) return true;
            if (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1)) return true;
        }
        return false;
    }


    /**
     * 获取当前正在运行的方法的名称
     * @return
     */
    public static String getCurrentExecuteMethodName()  {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String method = null;
        if (stack != null) {
            int index = 0;
            if (stack.length > 1) {
                index = 1;
            }
            method = stack[index].getMethodName();
        }
        return method;
    }

}
