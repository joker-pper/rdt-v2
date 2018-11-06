package com.devloper.joker.redundant.fill;

import com.devloper.joker.redundant.model.Column;
import com.devloper.joker.redundant.model.ModifyCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FillRSModel {

    private Map<Class, List<FillKeyModel>> fillKeyModelListMap = new HashMap<Class, List<FillKeyModel>>(16);
    private Map<Class, FillManyKeyModel> fillManyKeyModelMap = new HashMap<Class, FillManyKeyModel>(16);

    public Map<Class, List<FillKeyModel>> getFillKeyModelListMap() {
        return fillKeyModelListMap;
    }

    public void setFillKeyModelListMap(Map<Class, List<FillKeyModel>> fillKeyModelListMap) {
        this.fillKeyModelListMap = fillKeyModelListMap;
    }

    public Map<Class, FillManyKeyModel> getFillManyKeyModelMap() {
        return fillManyKeyModelMap;
    }

    public void setFillManyKeyModelMap(Map<Class, FillManyKeyModel> fillManyKeyModelMap) {
        this.fillManyKeyModelMap = fillManyKeyModelMap;
    }



    /**
     * 获取关于当前modifyCondition对应entityClass的FillKeyVO对象
     * @param entityClass
     * @param modifyCondition
     * @param entityFillKeyVoListMap
     * @return
     */
    public FillKeyModel getFillKeyModel(Class entityClass, ModifyCondition modifyCondition, Map<Class, List<FillKeyModel>> entityFillKeyVoListMap) {
        Column targetColumn = modifyCondition.getTargetColumn();
        List<FillKeyModel> fillKeyModelList = entityFillKeyVoListMap.get(entityClass);

        if (fillKeyModelList == null) {
            fillKeyModelList = new ArrayList<FillKeyModel>(16);
            entityFillKeyVoListMap.put(entityClass, fillKeyModelList);
        }

        FillKeyModel fillKeyModel = null;

        String targetProperty = targetColumn.getProperty();
        for (FillKeyModel vo : fillKeyModelList) {
            if (vo.getKey().equals(targetProperty)) {
                fillKeyModel = vo;
                break;
            }
        }

        if (fillKeyModel == null) {
            fillKeyModel = new FillKeyModel();
            fillKeyModel.setKey(targetProperty);
            fillKeyModel.setEntityClass(entityClass);
            fillKeyModel.setIsPrimaryKey(targetColumn.getIsPrimaryId());
            fillKeyModelList.add(fillKeyModel);
        }
        return fillKeyModel;
    }
}
