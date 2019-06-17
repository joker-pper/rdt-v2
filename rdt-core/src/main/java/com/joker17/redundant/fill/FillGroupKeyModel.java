package com.joker17.redundant.fill;


import com.joker17.redundant.model.Column;
import com.joker17.redundant.model.ModifyGroupDescribe;
import com.joker17.redundant.model.ModifyGroupKeysColumn;

import java.util.ArrayList;
import java.util.List;

public class FillGroupKeyModel extends FillKeyModel {

    private List<FillGroupKeyDetail> groupKeyDetails = new ArrayList<FillGroupKeyDetail>(16);

    public List<FillGroupKeyDetail> getGroupKeyDetails() {
        return groupKeyDetails;
    }

    public void setGroupKeyDetails(List<FillGroupKeyDetail> groupKeyDetails) {
        this.groupKeyDetails = groupKeyDetails;
    }

    public FillGroupKeyDetail initFillGroupKeyDetailData(ModifyGroupDescribe groupDescribe, Object data, List<Object> gainConditionValueList) {
        FillGroupKeyDetail result = null;
        ModifyGroupKeysColumn modifyGroupKeysColumn = groupDescribe.getModifyGroupKeysColumn();
        List<Column> gainConditionColumnList = modifyGroupKeysColumn.getGainConditionColumnList();

        for (FillGroupKeyDetail fillGroupKeyDetail : groupKeyDetails) {
            List<Column> currentGainConditionColumnList = modifyGroupKeysColumn.getGainConditionColumnList();

            if (fillGroupKeyDetail.getSelectColumnValue().equals(modifyGroupKeysColumn.getGainSelectColumn())
                    && RdtFillBuilder.equalsColumnElement(gainConditionColumnList, currentGainConditionColumnList)) {
                result = fillGroupKeyDetail;
                if (gainConditionValueList.size() > 1) {
                    //需要进行处理值顺序
                    List<Object> tempGainConditionValueList = new ArrayList<Object>(gainConditionValueList.size());
                    for (Column currentGainConditionColumn : currentGainConditionColumnList) {
                        for (int i = 0; i < gainConditionColumnList.size(); i++) {
                            if (gainConditionColumnList.get(i).equals(currentGainConditionColumn)) {
                                tempGainConditionValueList.add(gainConditionValueList.get(i));
                                break;
                            }
                        }
                    }
                    gainConditionValueList = tempGainConditionValueList;
                }
                break;
            }
        }

        if (result == null) {
            result = new FillGroupKeyDetail();
            result.setConditionColumnValues(modifyGroupKeysColumn.getGainConditionColumnList());
            result.setSelectColumnValue(modifyGroupKeysColumn.getGainSelectColumn());
        }
        result.initFillGroupKeyDetailData(gainConditionValueList, groupDescribe, data);
        return result;
    }





}
