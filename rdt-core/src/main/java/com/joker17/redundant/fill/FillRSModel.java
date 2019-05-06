package com.joker17.redundant.fill;

import com.joker17.redundant.model.ClassModel;
import com.joker17.redundant.model.Column;
import com.joker17.redundant.model.ModifyCondition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FillRSModel implements Serializable {

    private Map<Class, List<FillOneKeyModel>> fillKeyModelListMap = new HashMap<Class, List<FillOneKeyModel>>(16);
    private Map<Class, FillManyKeyModel> fillManyKeyModelMap = new HashMap<Class, FillManyKeyModel>(16);

    public Map<Class, List<FillOneKeyModel>> getFillKeyModelListMap() {
        return fillKeyModelListMap;
    }

    public void setFillKeyModelListMap(Map<Class, List<FillOneKeyModel>> fillKeyModelListMap) {
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
     * @param entityClassModel
     * @param modifyCondition
     * @param entityFillKeyVoListMap
     * @return
     */
    public FillOneKeyModel getFillKeyModel(ClassModel entityClassModel, ModifyCondition modifyCondition, Map<Class, List<FillOneKeyModel>> entityFillKeyVoListMap) {
        Column targetColumn = modifyCondition.getTargetColumn();
        Class entityClass = entityClassModel.getCurrentClass();
        List<FillOneKeyModel> fillOneKeyModelList = entityFillKeyVoListMap.get(entityClass);
        if (fillOneKeyModelList == null) {
            fillOneKeyModelList = new ArrayList<FillOneKeyModel>(16);
            entityFillKeyVoListMap.put(entityClass, fillOneKeyModelList);
        }

        FillOneKeyModel fillOneKeyModel = null;

        String targetProperty = targetColumn.getProperty();
        for (FillOneKeyModel vo : fillOneKeyModelList) {
            if (vo.getKey().equals(targetProperty)) {
                fillOneKeyModel = vo;
                break;
            }
        }

        if (fillOneKeyModel == null) {
            fillOneKeyModel = new FillOneKeyModel();
            fillOneKeyModel.setKey(targetProperty);
            fillOneKeyModel.setEntityClass(entityClass);
            fillOneKeyModel.setClassModel(entityClassModel);
            fillOneKeyModel.setKeyColumn(targetColumn);
            fillOneKeyModelList.add(fillOneKeyModel);
        }
        return fillOneKeyModel;
    }
}
