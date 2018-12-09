package com.joker17.rdt_sbm;

import com.alibaba.fastjson.JSONObject;
import com.joker17.redundant.support.Prototype;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class CriteriaMarkTest {


    protected String getPropertyMark(String property, String targetProperty) {
        if (true) {
            return property + "->" + targetProperty;
        }
        return property;
    }



    protected Map<String, Object> getParsedCriteriaMark(Criteria criteria, Map<String, String> conditionPropertyMap) {
        Map criteriaObjectMap = Prototype.of(criteria.getCriteriaObject()).deepClone().getModel();
        Map<String, Object> currentConditionLogMap = new LinkedHashMap<>(criteriaObjectMap);
        parseCriteriaMark(currentConditionLogMap, false, conditionPropertyMap);
        return currentConditionLogMap;
    }




    protected void parseCriteriaMark(Map<String, Object> valMap, boolean isElemMatch, Map<String, String> conditionPropertyMap) {
        if (valMap != null && !valMap.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iterator = valMap.entrySet().iterator();
            Map<String, Object> newMap = new LinkedHashMap<>(16);

            while (iterator.hasNext()) {
                Map.Entry<String, Object> currentEntry = iterator.next();
                String currentKey = currentEntry.getKey();
                Object currentValue = currentEntry.getValue();

                if (isElemMatch) {
                    String targetProperty = conditionPropertyMap.get(currentKey);
                    if (targetProperty != null) {
                        newMap.put(getPropertyMark(currentKey, targetProperty), valMap.get(currentKey));
                        iterator.remove();
                    }
                }
                if (currentValue instanceof Map) {
                    parseCriteriaMark((Map<String, Object>)currentValue, "$elemMatch".equals(currentKey), conditionPropertyMap);
                }

            }
            valMap.putAll(newMap);
        }

    }

    public static void main(String[] args) {

        Criteria criteria = new Criteria();
        criteria.and("results").elemMatch(Criteria.where("userId").is("222").and("username").is("joker").elemMatch(Criteria.where("mark").is(1)
                .elemMatch(Criteria.where("mark").is(1)))).and("username").is("@323");
        Map<String, String> conditionPropertyMap = new HashMap<>();
        conditionPropertyMap.put("userId", "id1");
        conditionPropertyMap.put("username", "username1");
        conditionPropertyMap.put("mark", "mark1");
        System.out.println(JSONObject.toJSONString(criteria.getCriteriaObject()));

        System.out.println(JSONObject.toJSONString(new CriteriaMarkTest().getParsedCriteriaMark(criteria, conditionPropertyMap)));

    }

}
