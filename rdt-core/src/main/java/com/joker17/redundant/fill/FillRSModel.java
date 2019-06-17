package com.joker17.redundant.fill;

import com.joker17.redundant.model.ClassModel;
import com.joker17.redundant.model.Column;
import com.joker17.redundant.model.ModifyCondition;
import com.joker17.redundant.model.ModifyGroupKeysColumn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FillRSModel implements Serializable {

    private Map<Class, List<FillOneKeyModel>> fillKeyModelListMap = new HashMap<Class, List<FillOneKeyModel>>(16);

    private Map<Class, FillManyKeyModel> fillManyKeyModelMap = new HashMap<Class, FillManyKeyModel>(16);

    private Map<Class, FillGroupKeyModel> fillGroupKeyModelMap = new HashMap<Class, FillGroupKeyModel>(16);


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

    public Map<Class, FillGroupKeyModel> getFillGroupKeyModelMap() {
        return fillGroupKeyModelMap;
    }

    public void setFillGroupKeyModelMap(Map<Class, FillGroupKeyModel> fillGroupKeyModelMap) {
        this.fillGroupKeyModelMap = fillGroupKeyModelMap;
    }

    /**
     * 获取关于当前modifyCondition对应entityClass的FillKeyVO对象
     * @param entityClassModel
     * @param modifyCondition
     * @return
     */
    public FillOneKeyModel getFillKeyModel(ClassModel entityClassModel, ModifyCondition modifyCondition) {
        Column targetColumn = modifyCondition.getTargetColumn();
        return getFillKeyModel(entityClassModel, targetColumn);
    }

    public FillOneKeyModel getFillKeyModel(ClassModel entityClassModel, ModifyGroupKeysColumn modifyGroupKeysColumn) {
        Column targetColumn = modifyGroupKeysColumn.getTargetColumn();
        return getFillKeyModel(entityClassModel, targetColumn);
    }

    protected FillOneKeyModel getFillKeyModel(ClassModel entityClassModel, Column targetColumn) {
        Class entityClass = entityClassModel.getCurrentClass();
        List<FillOneKeyModel> fillOneKeyModelList = fillKeyModelListMap.get(entityClass);
        if (fillOneKeyModelList == null) {
            fillOneKeyModelList = new ArrayList<FillOneKeyModel>(16);
            fillKeyModelListMap.put(entityClass, fillOneKeyModelList);
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
            fillOneKeyModel.setIsPrimaryKey(targetColumn.getIsPrimaryId());
            fillOneKeyModelList.add(fillOneKeyModel);
        }
        return fillOneKeyModel;
    }

    public FillGroupKeyModel getFillGroupKeyModel(ClassModel gainClassModel) {
        Class entityClass = gainClassModel.getCurrentClass();
        FillGroupKeyModel fillGroupKeyModel = fillGroupKeyModelMap.get(entityClass);
        if (fillGroupKeyModel == null) {
            fillGroupKeyModel = new FillGroupKeyModel();
            fillGroupKeyModel.setEntityClass(entityClass);
            fillGroupKeyModel.setClassModel(gainClassModel);
        }
        return fillGroupKeyModel;
    }
}
