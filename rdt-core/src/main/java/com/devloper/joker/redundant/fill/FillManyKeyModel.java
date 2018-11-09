package com.devloper.joker.redundant.fill;



import java.util.*;

/**
 * 作为base class(即被引用的字段所处的实体类)多项key列的实体
 */

public class FillManyKeyModel extends FillKeyModel {

    /**
     * 关于当前base class多组条件列的数据
     */
    private List<FillManyKeyDetail> manyKeyDetails = new ArrayList<FillManyKeyDetail>(16);


    public List<FillManyKeyDetail> getManyKeyDetails() {
        return manyKeyDetails;
    }

    public void setManyKeyDetails(List<FillManyKeyDetail> manyKeyDetails) {
        this.manyKeyDetails = manyKeyDetails;
    }


}
