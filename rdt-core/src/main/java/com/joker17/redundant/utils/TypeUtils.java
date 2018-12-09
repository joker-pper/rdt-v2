package com.joker17.redundant.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class TypeUtils {
    public static boolean compatibleWithJavaBean = false;
    /** 根据field name的大小写输出输入数据 */
    public static boolean compatibleWithFieldName = false;
    private static boolean setAccessibleEnable = true;
    private static boolean oracleTimestampMethodInited = false;
    private static Method oracleTimestampMethod;
    private static boolean oracleDateMethodInited = false;
    private static Method oracleDateMethod;
    private static boolean optionalClassInited = false;
    private static Class<?> optionalClass;
    private static boolean transientClassInited = false;
    private static Class<? extends Annotation> transientClass;

    private static Class<? extends Annotation> class_OneToMany = null;
    private static boolean class_OneToMany_error = false;
    private static Class<? extends Annotation> class_ManyToMany = null;
    private static boolean class_ManyToMany_error = false;

    private static Method method_HibernateIsInitialized = null;
    private static boolean method_HibernateIsInitialized_error = false;
    private static volatile Class kotlin_metadata;
    private static volatile boolean kotlin_metadata_error;
    private static volatile boolean kotlin_class_klass_error;
    private static volatile Constructor kotlin_kclass_constructor;
    private static volatile Method kotlin_kclass_getConstructors;
    private static volatile Method kotlin_kfunction_getParameters;
    private static volatile Method kotlin_kparameter_getName;
    private static volatile boolean kotlin_error;
    private static volatile Map<Class,String[]> kotlinIgnores;
    private static volatile boolean kotlinIgnores_error;
    private static ConcurrentMap<String,Class<?>> mappings = new ConcurrentHashMap<String,Class<?>>(16, 0.75f, 1);
    private static Class<?> pathClass;
    private static boolean pathClass_error = false;

    static{
        addBaseClassMappings();
    }

    public static String castToString(Object value){
        if(value == null){
            return null;
        }
        return value.toString();
    }

    public static Byte castToByte(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).byteValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            return Byte.parseByte(strVal);
        }
        throw new IllegalArgumentException("can not cast to byte, value : " + value);
    }

    public static Character castToChar(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Character){
            return (Character) value;
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0){
                return null;
            }
            if(strVal.length() != 1){
                throw new IllegalArgumentException("can not cast to char, value : " + value);
            }
            return strVal.charAt(0);
        }
        throw new IllegalArgumentException("can not cast to char, value : " + value);
    }

    public static Short castToShort(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).shortValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            return Short.parseShort(strVal);
        }
        throw new IllegalArgumentException("can not cast to short, value : " + value);
    }

    public static BigDecimal castToBigDecimal(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof BigDecimal){
            return (BigDecimal) value;
        }
        if(value instanceof BigInteger){
            return new BigDecimal((BigInteger) value);
        }
        String strVal = value.toString();
        if(strVal.length() == 0){
            return null;
        }
        if(value instanceof Map && ((Map) value).size() == 0){
            return null;
        }
        return new BigDecimal(strVal);
    }

    public static BigInteger castToBigInteger(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof BigInteger){
            return (BigInteger) value;
        }
        if(value instanceof Float || value instanceof Double){
            return BigInteger.valueOf(((Number) value).longValue());
        }
        String strVal = value.toString();
        if(strVal.length() == 0 //
                || "null".equals(strVal) //
                || "NULL".equals(strVal)){
            return null;
        }
        return new BigInteger(strVal);
    }

    public static Float castToFloat(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).floatValue();
        }
        if(value instanceof String){
            String strVal = value.toString();
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != 0){
                strVal = strVal.replaceAll(",", "");
            }
            return Float.parseFloat(strVal);
        }
        throw new IllegalArgumentException("can not cast to float, value : " + value);
    }

    public static Double castToDouble(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).doubleValue();
        }
        if(value instanceof String){
            String strVal = value.toString();
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != 0){
                strVal = strVal.replaceAll(",", "");
            }
            return Double.parseDouble(strVal);
        }
        throw new IllegalArgumentException("can not cast to double, value : " + value);
    }

    public static Date castToDate(Object value){
        return castToDate(value, null);
    }

    public static Date castToDate(Object value, String format){
        if(value == null){
            return null;
        }
        if(value instanceof Date){ // 使用频率最高的，应优先处理
            return (Date) value;
        }
        if(value instanceof Calendar){
            return ((Calendar) value).getTime();
        }
        long longValue = -1;
        if(value instanceof Number){
            longValue = ((Number) value).longValue();
            return new Date(longValue);
        }
        if(value instanceof String){
            String strVal = (String) value;

            if(strVal.startsWith("/Date(") && strVal.endsWith(")/")){
                strVal = strVal.substring(6, strVal.length() - 2);
            }
            if(strVal.indexOf('-') != -1){
                if (format == null) {
                    if (strVal.length() == "yyyy-MM-dd HH:mm:ss".length()) {
                        format = "yyyy-MM-dd HH:mm:ss";
                    } else if (strVal.length() == 22) {
                        format = "yyyyMMddHHmmssSSSZ";
                    } else if (strVal.length() == 10) {
                        format = "yyyy-MM-dd";
                    } else if (strVal.length() == "yyyy-MM-dd HH:mm:ss".length()) {
                        format = "yyyy-MM-dd HH:mm:ss";
                    } else if (strVal.length() == 29
                            && strVal.charAt(26) == ':'
                            && strVal.charAt(28) == '0') {
                        format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
                    } else {
                        format = "yyyy-MM-dd HH:mm:ss.SSS";
                    }
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getDefault());
                try{
                    return dateFormat.parse(strVal);
                } catch(ParseException e){
                    throw new IllegalArgumentException("can not cast to Date, value : " + strVal);
                }
            }
            if(strVal.length() == 0){
                return null;
            }
            longValue = Long.parseLong(strVal);
        }
        if(longValue < 0){
            Class<?> clazz = value.getClass();
            if("oracle.sql.TIMESTAMP".equals(clazz.getName())){
                if(oracleTimestampMethod == null && !oracleTimestampMethodInited){
                    try{
                        oracleTimestampMethod = clazz.getMethod("toJdbc");
                    } catch(NoSuchMethodException e){
                        // skip
                    } finally{
                        oracleTimestampMethodInited = true;
                    }
                }
                Object result;
                try{
                    result = oracleTimestampMethod.invoke(value);
                } catch(Exception e){
                    throw new IllegalArgumentException("can not cast oracle.sql.TIMESTAMP to Date", e);
                }
                return (Date) result;
            }
            if("oracle.sql.DATE".equals(clazz.getName())){
                if(oracleDateMethod == null && !oracleDateMethodInited){
                    try{
                        oracleDateMethod = clazz.getMethod("toJdbc");
                    } catch(NoSuchMethodException e){
                        // skip
                    } finally{
                        oracleDateMethodInited = true;
                    }
                }
                Object result;
                try{
                    result = oracleDateMethod.invoke(value);
                } catch(Exception e){
                    throw new IllegalArgumentException("can not cast oracle.sql.DATE to Date", e);
                }
                return (Date) result;
            }
            throw new IllegalArgumentException("can not cast to Date, value : " + value);
        }
        return new Date(longValue);
    }

    public static java.sql.Date castToSqlDate(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof java.sql.Date){
            return (java.sql.Date) value;
        }
        if(value instanceof Date){
            return new java.sql.Date(((Date) value).getTime());
        }
        if(value instanceof Calendar){
            return new java.sql.Date(((Calendar) value).getTimeInMillis());
        }
        long longValue = 0;
        if(value instanceof Number){
            longValue = ((Number) value).longValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(isNumber(strVal)){
                longValue = Long.parseLong(strVal);
            }
        }
        if(longValue <= 0){
            throw new IllegalArgumentException("can not cast to Date, value : " + value); // TODO 忽略 1970-01-01 之前的时间处理？
        }
        return new java.sql.Date(longValue);
    }

    public static java.sql.Time castToSqlTime(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof java.sql.Time){
            return (java.sql.Time) value;
        }
        if(value instanceof Date){
            return new java.sql.Time(((Date) value).getTime());
        }
        if(value instanceof Calendar){
            return new java.sql.Time(((Calendar) value).getTimeInMillis());
        }
        long longValue = 0;
        if(value instanceof Number){
            longValue = ((Number) value).longValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equalsIgnoreCase(strVal)){
                return null;
            }
            if(isNumber(strVal)){
                longValue = Long.parseLong(strVal);
            }
        }
        if(longValue <= 0){
            throw new IllegalArgumentException("can not cast to Date, value : " + value); // TODO 忽略 1970-01-01 之前的时间处理？
        }
        return new java.sql.Time(longValue);
    }

    public static java.sql.Timestamp castToTimestamp(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Calendar){
            return new java.sql.Timestamp(((Calendar) value).getTimeInMillis());
        }
        if(value instanceof java.sql.Timestamp){
            return (java.sql.Timestamp) value;
        }
        if(value instanceof Date){
            return new java.sql.Timestamp(((Date) value).getTime());
        }
        long longValue = 0;
        if(value instanceof Number){
            longValue = ((Number) value).longValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.endsWith(".000000000")){
                strVal = strVal.substring(0, strVal.length() - 10);
            } else if(strVal.endsWith(".000000")){
                strVal = strVal.substring(0, strVal.length() - 7);
            }
            if(isNumber(strVal)){
                longValue = Long.parseLong(strVal);
            }
        }
        if(longValue <= 0){
            throw new IllegalArgumentException("can not cast to Timestamp, value : " + value);
        }
        return new java.sql.Timestamp(longValue);
    }

    public static boolean isNumber(String str){
        for(int i = 0; i < str.length(); ++i){
            char ch = str.charAt(i);
            if(ch == '+' || ch == '-'){
                if(i != 0){
                    return false;
                }
            } else if(ch < '0' || ch > '9'){
                return false;
            }
        }
        return true;
    }

    public static Long castToLong(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).longValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != 0){
                strVal = strVal.replaceAll(",", "");
            }
            try{
                return Long.parseLong(strVal);
            } catch(NumberFormatException ex){
                //
            }

        }
        if(value instanceof Map){
            Map map = (Map) value;
            if(map.size() == 2
                    && map.containsKey("andIncrement")
                    && map.containsKey("andDecrement")){
                Iterator iter = map.values().iterator();
                iter.next();
                Object value2 = iter.next();
                return castToLong(value2);
            }
        }
        throw new IllegalArgumentException("can not cast to long, value : " + value);
    }

    public static Integer castToInt(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Integer){
            return (Integer) value;
        }
        if(value instanceof Number){
            return ((Number) value).intValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != 0){
                strVal = strVal.replaceAll(",", "");
            }
            return Integer.parseInt(strVal);
        }
        if(value instanceof Boolean){
            return ((Boolean) value).booleanValue() ? 1 : 0;
        }
        if(value instanceof Map){
            Map map = (Map) value;
            if(map.size() == 2
                    && map.containsKey("andIncrement")
                    && map.containsKey("andDecrement")){
                Iterator iter = map.values().iterator();
                iter.next();
                Object value2 = iter.next();
                return castToInt(value2);
            }
        }
        throw new IllegalArgumentException("can not cast to int, value : " + value);
    }

    public static byte[] castToBytes(Object value){
        if(value instanceof byte[]){
            return (byte[]) value;
        }

        throw new IllegalArgumentException("can not cast to int, value : " + value);
    }

    public static Boolean castToBoolean(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Boolean){
            return (Boolean) value;
        }
        if(value instanceof Number){
            return ((Number) value).intValue() == 1;
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if("true".equalsIgnoreCase(strVal) //
                    || "1".equals(strVal)){
                return Boolean.TRUE;
            }
            if("false".equalsIgnoreCase(strVal) //
                    || "0".equals(strVal)){
                return Boolean.FALSE;
            }
            if("Y".equalsIgnoreCase(strVal) //
                    || "T".equals(strVal)){
                return Boolean.TRUE;
            }
            if("F".equalsIgnoreCase(strVal) //
                    || "N".equals(strVal)){
                return Boolean.FALSE;
            }
        }
        throw new IllegalArgumentException("can not cast to boolean, value : " + value);
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T cast(Object obj, Class<T> clazz){
        if(obj == null){
            if(clazz == int.class){
                return (T) Integer.valueOf(0);
            } else if(clazz == long.class){
                return (T) Long.valueOf(0);
            } else if(clazz == short.class){
                return (T) Short.valueOf((short) 0);
            } else if(clazz == byte.class){
                return (T) Byte.valueOf((byte) 0);
            } else if(clazz == float.class){
                return (T) Float.valueOf(0);
            } else if(clazz == double.class){
                return (T) Double.valueOf(0);
            } else if(clazz == boolean.class){
                return (T) Boolean.FALSE;
            }
            return null;
        }
        if(clazz == null){
            throw new IllegalArgumentException("clazz is null");
        }
        if(clazz == obj.getClass()){
            return (T) obj;
        }
        if(obj instanceof Map){
            if(clazz == Map.class){
                return (T) obj;
            }
        }
        if(clazz.isArray()){
            if(obj instanceof Collection){
                Collection collection = (Collection) obj;
                int index = 0;
                Object array = Array.newInstance(clazz.getComponentType(), collection.size());
                for(Object item : collection){
                    Object value = cast(item, clazz.getComponentType());
                    Array.set(array, index, value);
                    index++;
                }
                return (T) array;
            }
            if(clazz == byte[].class){
                return (T) castToBytes(obj);
            }
        }
        if(clazz.isAssignableFrom(obj.getClass())){
            return (T) obj;
        }
        if(clazz == boolean.class || clazz == Boolean.class){
            return (T) castToBoolean(obj);
        }
        if(clazz == byte.class || clazz == Byte.class){
            return (T) castToByte(obj);
        }
        if(clazz == char.class || clazz == Character.class){
            return (T) castToChar(obj);
        }
        if(clazz == short.class || clazz == Short.class){
            return (T) castToShort(obj);
        }
        if(clazz == int.class || clazz == Integer.class){
            return (T) castToInt(obj);
        }
        if(clazz == long.class || clazz == Long.class){
            return (T) castToLong(obj);
        }
        if(clazz == float.class || clazz == Float.class){
            return (T) castToFloat(obj);
        }
        if(clazz == double.class || clazz == Double.class){
            return (T) castToDouble(obj);
        }
        if(clazz == String.class){
            return (T) castToString(obj);
        }
        if(clazz == BigDecimal.class){
            return (T) castToBigDecimal(obj);
        }
        if(clazz == BigInteger.class){
            return (T) castToBigInteger(obj);
        }
        if(clazz == Date.class){
            return (T) castToDate(obj);
        }
        if(clazz == java.sql.Date.class){
            return (T) castToSqlDate(obj);
        }
        if(clazz == java.sql.Time.class){
            return (T) castToSqlTime(obj);
        }
        if(clazz == java.sql.Timestamp.class){
            return (T) castToTimestamp(obj);
        }

        if(Calendar.class.isAssignableFrom(clazz)){
            Date date = castToDate(obj);
            Calendar calendar;
            if(clazz == Calendar.class){
                calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
            } else{
                try{
                    calendar = (Calendar) clazz.newInstance();
                } catch(Exception e){
                    throw new IllegalArgumentException("can not cast to : " + clazz.getName(), e);
                }
            }
            calendar.setTime(date);
            return (T) calendar;
        }


        if(obj instanceof String){
            String strVal = (String) obj;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(clazz == Currency.class){
                return (T) Currency.getInstance(strVal);
            }
            if(clazz == Locale.class){
                return (T) toLocale(strVal);
            }
        }
        throw new IllegalArgumentException("can not cast to : " + clazz.getName());
    }

    public static Locale toLocale(String strVal){
        String[] items = strVal.split("_");
        if(items.length == 1){
            return new Locale(items[0]);
        }
        if(items.length == 2){
            return new Locale(items[0], items[1]);
        }
        return new Locale(items[0], items[1], items[2]);
    }


    private static void addBaseClassMappings(){
        mappings.put("byte", byte.class);
        mappings.put("short", short.class);
        mappings.put("int", int.class);
        mappings.put("long", long.class);
        mappings.put("float", float.class);
        mappings.put("double", double.class);
        mappings.put("boolean", boolean.class);
        mappings.put("char", char.class);
        mappings.put("[byte", byte[].class);
        mappings.put("[short", short[].class);
        mappings.put("[int", int[].class);
        mappings.put("[long", long[].class);
        mappings.put("[float", float[].class);
        mappings.put("[double", double[].class);
        mappings.put("[boolean", boolean[].class);
        mappings.put("[char", char[].class);
        mappings.put("[B", byte[].class);
        mappings.put("[S", short[].class);
        mappings.put("[I", int[].class);
        mappings.put("[J", long[].class);
        mappings.put("[F", float[].class);
        mappings.put("[D", double[].class);
        mappings.put("[C", char[].class);
        mappings.put("[Z", boolean[].class);
        Class<?>[] classes = new Class[]{
                Object.class,
                Cloneable.class,
                loadClass("java.lang.AutoCloseable"),
                Exception.class,
                RuntimeException.class,
                IllegalAccessError.class,
                IllegalAccessException.class,
                IllegalArgumentException.class,
                IllegalMonitorStateException.class,
                IllegalStateException.class,
                IllegalThreadStateException.class,
                IndexOutOfBoundsException.class,
                InstantiationError.class,
                InstantiationException.class,
                InternalError.class,
                InterruptedException.class,
                LinkageError.class,
                NegativeArraySizeException.class,
                NoClassDefFoundError.class,
                NoSuchFieldError.class,
                NoSuchFieldException.class,
                NoSuchMethodError.class,
                NoSuchMethodException.class,
                NullPointerException.class,
                NumberFormatException.class,
                OutOfMemoryError.class,
                SecurityException.class,
                StackOverflowError.class,
                StringIndexOutOfBoundsException.class,
                TypeNotPresentException.class,
                VerifyError.class,
                StackTraceElement.class,
                HashMap.class,
                Hashtable.class,
                TreeMap.class,
                IdentityHashMap.class,
                WeakHashMap.class,
                LinkedHashMap.class,
                HashSet.class,
                LinkedHashSet.class,
                TreeSet.class,
                java.util.concurrent.TimeUnit.class,
                ConcurrentHashMap.class,
                loadClass("java.util.concurrent.ConcurrentSkipListMap"),
                loadClass("java.util.concurrent.ConcurrentSkipListSet"),
                java.util.concurrent.atomic.AtomicInteger.class,
                java.util.concurrent.atomic.AtomicLong.class,
                Collections.EMPTY_MAP.getClass(),
                BitSet.class,
                Calendar.class,
                Date.class,
                Locale.class,
                UUID.class,
                java.sql.Time.class,
                java.sql.Date.class,
                java.sql.Timestamp.class,
                SimpleDateFormat.class,
        };
        for(Class clazz : classes){
            if(clazz == null){
                continue;
            }
            mappings.put(clazz.getName(), clazz);
        }
        String[] awt = new String[]{
                "java.awt.Rectangle",
                "java.awt.Point",
                "java.awt.Font",
                "java.awt.Color"};
        for(String className : awt){
            Class<?> clazz = loadClass(className);
            if(clazz == null){
                break;
            }
            mappings.put(clazz.getName(), clazz);
        }
        String[] spring = new String[]{
                "org.springframework.util.LinkedMultiValueMap",
                "org.springframework.util.LinkedCaseInsensitiveMap",
                "org.springframework.remoting.support.RemoteInvocation",
                "org.springframework.remoting.support.RemoteInvocationResult",
                "org.springframework.security.web.savedrequest.DefaultSavedRequest",
                "org.springframework.security.web.savedrequest.SavedCookie",
                "org.springframework.security.web.csrf.DefaultCsrfToken",
                "org.springframework.security.web.authentication.WebAuthenticationDetails",
                "org.springframework.security.core.context.SecurityContextImpl",
                "org.springframework.security.authentication.UsernamePasswordAuthenticationToken",
                "org.springframework.security.core.authority.SimpleGrantedAuthority",
                "org.springframework.security.core.userdetails.User"
        };
        for(String className : spring){
            Class<?> clazz = loadClass(className);
            if(clazz == null){
                break;
            }
            mappings.put(clazz.getName(), clazz);
        }
    }

    public static void clearClassMapping(){
        mappings.clear();
        addBaseClassMappings();
    }

    public static Class<?> loadClass(String className){
        return loadClass(className, null);
    }

    public static boolean isPath(Class<?> clazz){
        if(pathClass == null && !pathClass_error){
            try{
                pathClass = Class.forName("java.nio.file.Path");
            } catch(Throwable ex){
                pathClass_error = true;
            }
        }
        if(pathClass != null){
            return pathClass.isAssignableFrom(clazz);
        }
        return false;
    }

    public static Class<?> getClassFromMapping(String className){
        return mappings.get(className);
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader) {
        return loadClass(className, classLoader, true);
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader, boolean cache) {
        if(className == null || className.length() == 0){
            return null;
        }
        Class<?> clazz = mappings.get(className);
        if(clazz != null){
            return clazz;
        }
        if(className.charAt(0) == '['){
            Class<?> componentType = loadClass(className.substring(1), classLoader);
            return Array.newInstance(componentType, 0).getClass();
        }
        if(className.startsWith("L") && className.endsWith(";")){
            String newClassName = className.substring(1, className.length() - 1);
            return loadClass(newClassName, classLoader);
        }
        try{
            if(classLoader != null){
                clazz = classLoader.loadClass(className);
                if (cache) {
                    mappings.put(className, clazz);
                }
                return clazz;
            }
        } catch(Throwable e){
            e.printStackTrace();
            // skip
        }
        try{
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if(contextClassLoader != null && contextClassLoader != classLoader){
                clazz = contextClassLoader.loadClass(className);
                if (cache) {
                    mappings.put(className, clazz);
                }
                return clazz;
            }
        } catch(Throwable e){
            // skip
        }
        try{
            clazz = Class.forName(className);
            mappings.put(className, clazz);
            return clazz;
        } catch(Throwable e){
            // skip
        }
        return clazz;
    }


    public static String decapitalize(String name){
        if(name == null || name.length() == 0){
            return name;
        }
        if(name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))){
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    static void setAccessible(AccessibleObject obj){
        if(!setAccessibleEnable){
            return;
        }
        if(obj.isAccessible()){
            return;
        }
        try{
            obj.setAccessible(true);
        } catch(AccessControlException error){
            setAccessibleEnable = false;
        }
    }

    public static Type getCollectionItemType(Type fieldType){
        Type itemType = null;
        Class<?> clazz;
        if(fieldType instanceof ParameterizedType){
            Type actualTypeArgument = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            if(actualTypeArgument instanceof WildcardType){
                WildcardType wildcardType = (WildcardType) actualTypeArgument;
                Type[] upperBounds = wildcardType.getUpperBounds();
                if(upperBounds.length == 1){
                    actualTypeArgument = upperBounds[0];
                }
            }
            itemType = actualTypeArgument;
        } else if(fieldType instanceof Class<?> //
                && !(clazz = (Class<?>) fieldType).getName().startsWith("java.")){
            Type superClass = clazz.getGenericSuperclass();
            itemType = TypeUtils.getCollectionItemType(superClass);
        }
        if(itemType == null){
            itemType = Object.class;
        }
        return itemType;
    }

    public static Class<?> getCollectionItemClass(Type fieldType){
        if(fieldType instanceof ParameterizedType){
            Class<?> itemClass;
            Type actualTypeArgument = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            if(actualTypeArgument instanceof WildcardType){
                WildcardType wildcardType = (WildcardType) actualTypeArgument;
                Type[] upperBounds = wildcardType.getUpperBounds();
                if(upperBounds.length == 1){
                    actualTypeArgument = upperBounds[0];
                }
            }
            if(actualTypeArgument instanceof Class){
                itemClass = (Class<?>) actualTypeArgument;
                if(!Modifier.isPublic(itemClass.getModifiers())){
                    throw new IllegalArgumentException("can not create ASMParser");
                }
            } else{
                throw new IllegalArgumentException("can not create ASMParser");
            }
            return itemClass;
        }
        return Object.class;
    }

    public static Type checkPrimitiveArray(GenericArrayType genericArrayType) {
        Type clz = genericArrayType;
        Type genericComponentType  = genericArrayType.getGenericComponentType();

        String prefix = "[";
        while (genericComponentType instanceof GenericArrayType) {
            genericComponentType = ((GenericArrayType) genericComponentType)
                    .getGenericComponentType();
            prefix += prefix;
        }

        if (genericComponentType instanceof Class<?>) {
            Class<?> ck = (Class<?>) genericComponentType;
            if (ck.isPrimitive()) {
                try {
                    if (ck == boolean.class) {
                        clz = Class.forName(prefix + "Z");
                    } else if (ck == char.class) {
                        clz = Class.forName(prefix + "C");
                    } else if (ck == byte.class) {
                        clz = Class.forName(prefix + "B");
                    } else if (ck == short.class) {
                        clz = Class.forName(prefix + "S");
                    } else if (ck == int.class) {
                        clz = Class.forName(prefix + "I");
                    } else if (ck == long.class) {
                        clz = Class.forName(prefix + "J");
                    } else if (ck == float.class) {
                        clz = Class.forName(prefix + "F");
                    } else if (ck == double.class) {
                        clz = Class.forName(prefix + "D");
                    }
                } catch (ClassNotFoundException e) {
                }
            }
        }

        return clz;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Collection createCollection(Type type) {
        Class<?> rawClass = getRawClass(type);
        Collection list;
        if(rawClass == AbstractCollection.class //
                || rawClass == Collection.class){
            list = new ArrayList();
        } else if(rawClass.isAssignableFrom(HashSet.class)){
            list = new HashSet();
        } else if(rawClass.isAssignableFrom(LinkedHashSet.class)){
            list = new LinkedHashSet();
        } else if(rawClass.isAssignableFrom(TreeSet.class)){
            list = new TreeSet();
        } else if(rawClass.isAssignableFrom(ArrayList.class)){
            list = new ArrayList();
        } else if(rawClass.isAssignableFrom(EnumSet.class)){
            Type itemType;
            if(type instanceof ParameterizedType){
                itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else{
                itemType = Object.class;
            }
            list = EnumSet.noneOf((Class<Enum>) itemType);
        } else{
            try{
                list = (Collection) rawClass.newInstance();
            } catch(Exception e){
                throw new IllegalArgumentException("create instance error, class " + rawClass.getName());
            }
        }
        return list;
    }

    public static Class<?> getRawClass(Type type){
        if(type instanceof Class<?>){
            return (Class<?>) type;
        } else if(type instanceof ParameterizedType){
            return getRawClass(((ParameterizedType) type).getRawType());
        } else{
            throw new IllegalArgumentException("TODO");
        }
    }

    public static boolean isProxy(Class<?> clazz){
        for(Class<?> item : clazz.getInterfaces()){
            String interfaceName = item.getName();
            if(interfaceName.equals("net.sf.cglib.proxy.Factory") //
                    || interfaceName.equals("org.springframework.cglib.proxy.Factory")){
                return true;
            }
            if(interfaceName.equals("javassist.util.proxy.ProxyObject") //
                    || interfaceName.equals("org.apache.ibatis.javassist.util.proxy.ProxyObject")
                    ){
                return true;
            }
        }
        return false;
    }

    public static boolean isTransient(Method method){
        if(method == null){
            return false;
        }
        if(!transientClassInited){
            try{
                transientClass = (Class<? extends Annotation>) Class.forName("java.beans.Transient");
            } catch(Exception e){
                // skip
            } finally{
                transientClassInited = true;
            }
        }
        if(transientClass != null){
            Annotation annotation = method.getAnnotation(transientClass);
            return annotation != null;
        }
        return false;
    }

    public static boolean isAnnotationPresentOneToMany(Method method) {
        if (method == null) {
            return false;
        }

        if (class_OneToMany == null && !class_OneToMany_error) {
            try {
                class_OneToMany = (Class<? extends Annotation>) Class.forName("javax.persistence.OneToMany");
            } catch (Throwable e) {
                // skip
                class_OneToMany_error = true;
            }
        }
        return class_OneToMany != null && method.isAnnotationPresent(class_OneToMany);

    }

    public static boolean isAnnotationPresentManyToMany(Method method) {
        if (method == null) {
            return false;
        }

        if (class_ManyToMany == null && !class_ManyToMany_error) {
            try {
                class_ManyToMany = (Class<? extends Annotation>) Class.forName("javax.persistence.ManyToMany");
            } catch (Throwable e) {
                // skip
                class_ManyToMany_error = true;
            }
        }
        return class_ManyToMany != null && (method.isAnnotationPresent(class_OneToMany) || method.isAnnotationPresent(class_ManyToMany));

    }

    public static boolean isHibernateInitialized(Object object){
        if(object == null){
            return false;
        }
        if(method_HibernateIsInitialized == null && !method_HibernateIsInitialized_error){
            try{
                Class<?> class_Hibernate = Class.forName("org.hibernate.Hibernate");
                method_HibernateIsInitialized = class_Hibernate.getMethod("isInitialized", Object.class);
            } catch(Throwable e){
                // skip
                method_HibernateIsInitialized_error = true;
            }
        }
        if(method_HibernateIsInitialized != null){
            try{
                Boolean initialized = (Boolean) method_HibernateIsInitialized.invoke(null, object);
                return initialized.booleanValue();
            } catch(Throwable e){
                // skip
            }
        }
        return true;
    }

    public static long fnv1a_64_lower(String key){
        long hashCode = 0xcbf29ce484222325L;
        for(int i = 0; i < key.length(); ++i){
            char ch = key.charAt(i);
            if(ch == '_' || ch == '-'){
                continue;
            }
            if(ch >= 'A' && ch <= 'Z'){
                ch = (char) (ch + 32);
            }
            hashCode ^= ch;
            hashCode *= 0x100000001b3L;
        }
        return hashCode;
    }

    public static long fnv1a_64(String key){
        long hashCode = 0xcbf29ce484222325L;
        for(int i = 0; i < key.length(); ++i){
            char ch = key.charAt(i);
            hashCode ^= ch;
            hashCode *= 0x100000001b3L;
        }
        return hashCode;
    }

    public static boolean isKotlin(Class clazz) {
        if (kotlin_metadata == null && !kotlin_metadata_error) {
            try {
                kotlin_metadata = Class.forName("kotlin.Metadata");
            } catch (Throwable e) {
                kotlin_metadata_error = true;
            }
        }
        return kotlin_metadata != null && clazz.isAnnotationPresent(kotlin_metadata);
    }

    public static Constructor getKoltinConstructor(Constructor[] constructors){
        Constructor creatorConstructor = null;
        for(Constructor<?> constructor : constructors){
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if(parameterTypes.length > 0 && parameterTypes[parameterTypes.length - 1].getName().equals("kotlin.jvm.internal.DefaultConstructorMarker")){
                continue;
            }
            if(creatorConstructor != null && creatorConstructor.getParameterTypes().length >= parameterTypes.length){
                continue;
            }
            creatorConstructor = constructor;
        }
        return creatorConstructor;
    }

    public static String[] getKoltinConstructorParameters(Class clazz){
        if(kotlin_kclass_constructor == null && !kotlin_class_klass_error){
            try{
                Class class_kotlin_kclass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlin_kclass_constructor = class_kotlin_kclass.getConstructor(Class.class);
            } catch(Throwable e){
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_kclass_constructor == null){
            return null;
        }

        if (kotlin_kclass_getConstructors == null && !kotlin_class_klass_error) {
            try{
                Class class_kotlin_kclass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlin_kclass_getConstructors = class_kotlin_kclass.getMethod("getConstructors");
            } catch(Throwable e){
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_kfunction_getParameters == null && !kotlin_class_klass_error) {
            try{
                Class class_kotlin_kfunction = Class.forName("kotlin.reflect.KFunction");
                kotlin_kfunction_getParameters = class_kotlin_kfunction.getMethod("getParameters");
            } catch(Throwable e){
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_kparameter_getName == null && !kotlin_class_klass_error) {
            try{
                Class class_kotlinn_kparameter = Class.forName("kotlin.reflect.KParameter");
                kotlin_kparameter_getName = class_kotlinn_kparameter.getMethod("getName");
            } catch(Throwable e){
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_error){
            return null;
        }

        try{
            Object constructor = null;
            Object kclassImpl = kotlin_kclass_constructor.newInstance(clazz);
            Iterable it = (Iterable) kotlin_kclass_getConstructors.invoke(kclassImpl);
            for(Iterator iterator = it.iterator(); iterator.hasNext(); iterator.hasNext()){
                Object item = iterator.next();
                List parameters = (List) kotlin_kfunction_getParameters.invoke(item);
                if (constructor != null && parameters.size() == 0) {
                    continue;
                }
                constructor = item;
            }
            List parameters = (List) kotlin_kfunction_getParameters.invoke(constructor);
            String[] names = new String[parameters.size()];
            for(int i = 0; i < parameters.size(); i++){
                Object param = parameters.get(i);
                names[i] = (String) kotlin_kparameter_getName.invoke(param);
            }
            return names;
        } catch(Throwable e){
            e.printStackTrace();
            kotlin_error = true;
        }
        return null;
    }

    private static boolean isKotlinIgnore(Class clazz, String methodName) {
        if (kotlinIgnores == null && !kotlinIgnores_error) {
            try {
                Map<Class, String[]> map = new HashMap<Class, String[]>();
                Class charRangeClass = Class.forName("kotlin.ranges.CharRange");
                map.put(charRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class intRangeClass = Class.forName("kotlin.ranges.IntRange");
                map.put(intRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class longRangeClass = Class.forName("kotlin.ranges.LongRange");
                map.put(longRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class floatRangeClass = Class.forName("kotlin.ranges.ClosedFloatRange");
                map.put(floatRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class doubleRangeClass = Class.forName("kotlin.ranges.ClosedDoubleRange");
                map.put(doubleRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                kotlinIgnores = map;
            } catch (Throwable error) {
                kotlinIgnores_error = true;
            }
        }
        if (kotlinIgnores == null) {
            return false;
        }
        String[] ignores = kotlinIgnores.get(clazz);
        return ignores != null && Arrays.binarySearch(ignores, methodName) >= 0;
    }

    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass){
        A a = clazz.getAnnotation(annotationClass);
        if (a != null){
            return a;
        }

        if(clazz.getAnnotations().length > 0){
            for(Annotation annotation : clazz.getAnnotations()){
                a = annotation.annotationType().getAnnotation(annotationClass);
                if(a != null){
                    return a;
                }
            }
        }
        return null;
    }
}
