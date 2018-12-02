package com.devloper.joker.redundant.support;

import com.devloper.joker.redundant.utils.PojoUtils;
import com.devloper.joker.redundant.utils.StringUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


public class DataSupport {
    public interface Callback {
        void execute(String property, Object data);
    }
    /**
     * 通过对象的属性表达式及回调函数进行执行结果(仅支持一维数组)
     * @param data simple object/array/list
     * @param property
     *                 * 时回调函数property为null
     *                 simple property  simple object的property
     *                 arr.*.key <==> arr.key
     *                 arr.[index].key
     *                 arr.* 会依次通过回调函数执行对应的结果值
     * @param callback 回调函数 property(为null时表示为当前对象) 属性 data 值
     */
    public static void dispose(Object data, String property, Callback callback) {
        dispose(data, property, null, callback);
    }

    private static void dispose(Object data, String property, String prefix, Callback callback) {
        String[] propertys = new String[0];
        String currentProperty;

        if (prefix == null) prefix = "";
        Object result;

        if (data != null && StringUtils.isNotEmpty(property)) {
            propertys = property.split("\\.");
            currentProperty = propertys[0];

            if (!"*".equals(currentProperty)) {
                prefix += currentProperty + ".";
                if (data instanceof Map) result = ((Map) data).get(currentProperty);
                else {
                    try {
                        Field field = PojoUtils.getDeclaredField(data.getClass(), currentProperty);
                        Method readMethod = PojoUtils.getReadMethod(field);
                        if (readMethod != null) result = PojoUtils.getMethodValue(readMethod, data);
                        else  result = PojoUtils.getFieldValue(field, data);
                    } catch (NoSuchFieldException e) {
                        throw new IllegalArgumentException(e.getMessage() + ", cause by " + prefix.substring(0, prefix.length() - 1));
                    }
                }
            } else {
                result = data;
            }
        } else {
            result = data;
        }

        if (result == null || propertys.length <= 1){
            if (StringUtils.isEmpty(prefix)) prefix = null;
            else prefix = prefix.substring(0, prefix.length() - 1);

            //当前result即为要访问最终的属性值
            if (callback != null && (prefix == null || property == null || property.equals("*") || prefix.contains(property))) callback.execute(prefix, result);
            //if (callback != null) callback.execute(prefix, result);
        } else {
            //读取下一个属性值
            String nextProperty = property.substring(property.indexOf(".") + 1);
            Long position = null;
            if (nextProperty.matches("\\[\\d+\\].*")) {
                int index = nextProperty.indexOf(".");

                String positionText;
                if (index > 0) {
                    positionText = nextProperty.substring(0, index);
                    nextProperty = nextProperty.substring(index + 1);
                } else {
                    positionText = nextProperty;
                    nextProperty = null;
                }

                positionText = positionText.replace("[", "").replace("]", "");
                position = Long.parseLong(positionText);
            }

            if (result != null && result.getClass().isArray()) {
                int size = Array.getLength(result);
                List<Object> resultTemp = new ArrayList<Object>(size);
                for (int i = 0; i < size; i ++) {
                    resultTemp.add(Array.get(result, i));
                }
                result = resultTemp;
            }

            //验证.*必须为集合
            /*if (nextProperty != null && nextProperty.matches("\\*.*")) {
                if (!(result instanceof Collection))
                    throw new IllegalArgumentException("only collection can use * "+ ", cause by " + prefix + nextProperty);
            }*/

            if (result instanceof Collection) {
                int size = ((Collection) result).size();
                if (position != null && position + 1 > size) {
                    throw new ArrayIndexOutOfBoundsException("max index is " + (size - 1) + ", cause by " + prefix + "[" + position ++ + "]");
                }
                int index = 0;
                boolean breakFlag = false;
                for (Object current : (Collection) result) {
                    if (position != null) {
                        if (position != index ++) continue;
                        else {
                            index --;
                            breakFlag = true;
                        }
                    }
                    dispose(current, nextProperty, prefix + "[" + index ++ + "].", callback);
                    if (breakFlag) break;
                }
            } else {
                dispose(result, nextProperty, prefix,  callback);
            }
        }
    }


    public static void main(String[] args) {
        Map<String, List<Long>> resultMap = new HashMap<String, List<Long>>();

        resultMap.put("value1", Arrays.asList(10L, 20L));
        resultMap.put("value2", Arrays.asList(10L));

        DataSupport.dispose(resultMap, "value1", new DataSupport.Callback() {
            @Override
            public void execute(String property, Object data) {
                System.out.println(property + ": " + data);
            }
        });

        DataSupport.dispose(resultMap, "value1.[0]", new DataSupport.Callback() {
            @Override
            public void execute(String property, Object data) {
                System.out.println(property + ": " + data);
            }
        });

        DataSupport.dispose(resultMap, "value1.*", new DataSupport.Callback() {
            @Override
            public void execute(String property, Object data) {
                System.out.println(property + ": " + data);
            }
        });

        DataSupport.dispose(resultMap, "value2.[0]", new DataSupport.Callback() {
            @Override
            public void execute(String property, Object data) {
                System.out.println(property + ": " + data);
            }
        });
    }

}
