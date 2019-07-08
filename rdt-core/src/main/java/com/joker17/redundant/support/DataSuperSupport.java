package com.joker17.redundant.support;

import com.joker17.redundant.utils.PojoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author joker-pper
 */
public class DataSuperSupport implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DataSuperSupport.class);

    public interface Callback {
        /**
         * 访达指定属性数据
         * @param sourceAccessProperty
         * @param source
         * @param resultAccessProperty
         * @param result
         */
        void arrive(String sourceAccessProperty, Object source, String resultAccessProperty, Object result);

        /**
         * @param sourceAccessProperty
         * @param source
         * @param resultAccessProperty
         * @param result               验证的对象
         * @param status               验证结果
         * @param expression
         * @param property             验证关于result的property
         * @param value                property value
         */
        void check(String sourceAccessProperty, Object source, String resultAccessProperty, Object result, boolean status, Expression expression, String property, Object value);
    }

    /**
     * 支持带单层级属性递进约束条件的访问到对象的finalAccessProperty的方法
     * @param data 要访问的对象
     * @param finalAccessProperty 除了*外会默认处理为访问以.*结尾的值; 为*时: 如果不存在expression则直接访问到当前对象,反之验证所有条件
     * @param expressionList 可选 不为空时会进行check expression
     * @param callback
     */
    public static void doDisposeHandle(final Object data, final String finalAccessProperty, List<Expression> expressionList, final Callback callback) {
        doDisposeHandle(data, finalAccessProperty, null, expressionList, callback);
    }

    private static void doDisposeHandle(final Object data, String finalAccessProperty, String globalProperty, List<Expression> expressionList, final Callback callback) {
        if (data != null) {

            if (finalAccessProperty == null) {
                finalAccessProperty = "*";
                logger.debug("current finalAccessProperty is null and change value is *");
            }

            String accessProperty = finalAccessProperty;
            if (!finalAccessProperty.equals("*") && !finalAccessProperty.endsWith(".*")) {
                finalAccessProperty += ".*";
            }

            List<List<Expression>> mergeExpression = mergeExpression(expressionList);
            if (!mergeExpression.isEmpty()) {

                //when has expression, to check expression, next object key start with prev object key
                // and final access property start with last object key.(when key is * should special check)

                List<String> objectKeys = new ArrayList<String>();
                for (List<Expression> expressions : mergeExpression) {
                    for (Expression expression : expressions) {
                        objectKeys.add(getActualKey(expression.getKey()));
                        break;
                    }
                }

                int keySize = objectKeys.size();

                if (keySize > 1) {
                    for (int i = 0; i < keySize - 1; i ++) {
                        String key = objectKeys.get(i);
                        String nextKey = objectKeys.get(i + 1);

                        if (!key.equals("*")) {
                            if (!nextKey.startsWith(key)) {
                                throw new IllegalArgumentException("the expression key has error about next object key " + nextKey +
                                        " is not start with prev object key " + key + ", please use right order");
                            }
                        }
                    }
                }

                if (!finalAccessProperty.equals("*")) {
                    String lastKey = objectKeys.get(objectKeys.size() - 1);
                    if (!lastKey.equals("*") && !finalAccessProperty.startsWith(lastKey) || finalAccessProperty.equals(lastKey)) {
                        throw new IllegalArgumentException("the expression key has error about final access property " + accessProperty +
                                " with last object key " + lastKey + ",\nplease check final access property is not eq and should start with last object key ");
                    }
                }

            }

            doDisposeCore(data, finalAccessProperty, globalProperty, mergeExpression, 0, callback);
        }
    }

    private static String getActualKey(String value) {
        int last = value.lastIndexOf(".");
        if (last == -1) {
            return "*";
        } else {
            return value.substring(0, last);
        }
    }

    /**
     * @param data
     * @param finalAccessProperty 最终所要访问的属性
     * @param globalProperty      全局property,用于标识关于访问该对象的全部属性
     * @param expressionResults   访问对象属性之前的条件约束
     * @param index
     * @param callback
     */
    private static void doDisposeCore(final Object data, final String finalAccessProperty, final String globalProperty, final List<List<Expression>> expressionResults, final int index, final Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must be not null.");
        }
        List<Expression> expressionList = new ArrayList<Expression>();
        if (!expressionResults.isEmpty() && index <= expressionResults.size() - 1) {
            expressionList = expressionResults.get(index);
        }

        final List<Expression> finalExpressionList = expressionList;

        //获取当前data所要访问的属性
        String to;
        if (index == 0 && expressionResults.isEmpty()) {
            to = finalAccessProperty == null ? "*" : finalAccessProperty;
        } else {
            String beforeKey = null;
            if (index > 0) {
                beforeKey = getActualKey(expressionResults.get(index - 1).get(0).getKey());
            }

            //当前key
            String currentKey;

            if (!finalExpressionList.isEmpty()) {
                currentKey = getActualKey(finalExpressionList.get(0).getKey());
            } else {
                currentKey = finalAccessProperty == null ? "*" : finalAccessProperty;
            }

            if (beforeKey == null || beforeKey.equals("*")) {
                to = currentKey;
            } else {
                to = currentKey.replaceFirst(beforeKey + ".", "");
            }
        }

        if (!to.equals("*") && !to.endsWith(".*")) {
            to += ".*";
        }

        logger.debug("access property {} dispose with property {}", finalAccessProperty, globalProperty != null ? globalProperty + "." + to : to);

        DataSupport.dispose(data, to, new DataSupport.Callback() {
            @Override
            public void execute(String resultProperty, Object result) {
                if (globalProperty != null) {
                    if (resultProperty != null) {
                        resultProperty = globalProperty + "." + resultProperty;
                    } else {
                        resultProperty = globalProperty;
                    }
                }
                if (finalExpressionList.isEmpty()) {
                    //最终所要访问的子对象
                    callback.arrive(finalAccessProperty, data, resultProperty, result);
                } else {
                    //验证是否满足
                    boolean todo = true;
                    for (Expression expression : finalExpressionList) {

                        //获取当前条件的属性名称
                        String key = expression.getKey();
                        int last = key.lastIndexOf(".");

                        //获取当前所要验证的属性及值
                        String currentProperty = key.substring(last + 1);
                        Object currentPropertyValue = getPropertyValue(result, currentProperty);
                        todo = isTrue(expression, currentPropertyValue);

                        String checkProperty = "";
                        if (resultProperty != null) {
                            checkProperty = resultProperty + ".";
                        }
                        checkProperty += currentProperty;
                        callback.check(finalAccessProperty, data, checkProperty, result, todo, expression, currentProperty, currentPropertyValue);

                        if (!todo) {
                            break;
                        }
                    }
                    if (todo) {
                        doDisposeCore(result, finalAccessProperty, resultProperty, expressionResults, index + 1, callback);
                    }
                }
            }
        });
    }



    /**
     * 将一定顺序的expressionList进行处理(支持一定程度的无序)
     * e.g [{key: status, value: 1}, {key: id, value: 1}, {key: user.status, value: 1}]
     * return: [ [{key: status, value: 1}, {key: id, value: 1}], [{key: user.status, value: 1}] ]
     *
     * @param expressionList
     * @return
     */
    public static List<List<Expression>> mergeExpression(List<Expression> expressionList) {
        List<List<Expression>> result = new ArrayList<List<Expression>>();
        List<Integer> keySplitSizeList = new ArrayList<Integer>();
        List<List<Expression>> keySplitResult = new ArrayList<List<Expression>>();
        if (expressionList != null) {
            for (Expression expression : expressionList) {
                String key = expression.getKey();

                List<Expression> currentGroupData = null;

                if (!result.isEmpty()) {
                    //with mark to find group data
                    String mark = getActualKey(key);
                    for (List<Expression> list : result) {
                        for (Expression temp : list) {
                            if (mark.equals(getActualKey(temp.getKey()))) {
                                currentGroupData = list;
                                break;
                            }
                        }
                        if (currentGroupData != null) {
                            break;
                        }
                    }
                }

                if (currentGroupData == null) {
                    currentGroupData = new ArrayList<Expression>();
                    result.add(currentGroupData);

                    //确保有序
                    int keySplitSize = key.split("\\.").length;
                    int index = getAddIndexAsc(keySplitSizeList, keySplitSize);
                    keySplitSizeList.add(index, keySplitSize);
                    keySplitResult.add(index, currentGroupData);
                }

                currentGroupData.add(expression);
            }
        }

        return keySplitResult;
    }

    //根据indexList中存在的元素返回当前value所要添加的索引位置
    private static int getAddIndexAsc(List<Integer> indexList, int value) {
        if (indexList != null) {
            boolean addFirst = indexList.isEmpty() || indexList.get(0) > value;
            if (addFirst) {
                return 0;
            }
            int size = indexList.size();
            if (size == 1 || value >= indexList.get(size - 1)) {
                return size;
            }

            int low = 0;
            int high = size - 1;
            while (low <= high) {
                int middle = (low + high) / 2;
                int temp = indexList.get(middle);
                if (value == temp) {
                    //确保为相等元素后面的位置
                    int index;
                    for (index = middle + 1; index < size; index ++) {
                        if (value < indexList.get(index)) {
                            break;
                        }
                    }
                    return index;
                } else if (value < temp && middle > 0 && value > indexList.get(middle - 1)) {
                    return middle;
                } else if (value < temp) {
                    high = middle - 1;
                } else {
                    low = middle + 1;
                }
            }
        }
        return -1;
    }

    public static List<Integer> sort(List<Integer> value) {
        List<Integer> result = new ArrayList<Integer>();
        if (value != null) {
            for (Integer it : value) {
                if (it != null) {
                    int index = getAddIndexAsc(result, it);
                    result.add(index, it);
                }
            }
        }
        return result;
    }

    public static Object getPropertyValue(Object obj, String property) {
        return PojoUtils.getPropertyValue(obj, property);
    }

    public static boolean isTrue(Expression expression, Object value) {
        if (expression != null) {
            if (expression.isContains()) {
                return isMatchedContainsValue(expression.getValue(), value);
            }
            return isMatchedValue(expression.getValue(), value);
        }
        return false;
    }




    /**
     * 判断两个值是否匹配
     *
     * @param current
     * @param target
     * @return
     */
    public static boolean isMatchedValue(Object current, Object target) {
        if (current == null && target == null) {
            return true;
        }

        if (current != null) {
            return current.equals(target);
        }

        return false;
    }

    /**
     * 比较逻辑列的值是否匹配
     *
     * @param data
     * @param target
     * @return
     */
    public static boolean isMatchedContainsValue(Object data, Object target) {
        if (data == null && target == null) {
            return true;
        }
        if (data != null) {
            if (data instanceof Collection) {
                if (target instanceof Collection) {
                    return data.equals(target) || ((Collection) data).containsAll((Collection<?>) target);
                }
                return ((Collection) data).contains(target);
            }
            return data.equals(target);
        }
        return false;
    }
}
