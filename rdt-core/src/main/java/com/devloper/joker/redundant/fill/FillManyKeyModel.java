package com.devloper.joker.redundant.fill;



import java.util.*;

/**
 * 作为base class(即被引用的字段所处的实体类)多项key列的实体
 */

public class FillManyKeyModel {

    /**
     * 关于当前base class多组条件列的数据
     */
    private List<FillManyKeyDetail> manyKeyDetails = new ArrayList<FillManyKeyDetail>(16);

    /**
     * 当前base class
     */
    private Class entityClass;


    public List<FillManyKeyDetail> getManyKeyDetails() {
        return manyKeyDetails;
    }

    public void setManyKeyDetails(List<FillManyKeyDetail> manyKeyDetails) {
        this.manyKeyDetails = manyKeyDetails;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public static void main(String[] args) {

        List<String> arrays = new ArrayList<String>();
        arrays.add("1");
        arrays.add("2");
        List<String> arrays2 = Arrays.asList("1", "2");

        Map<List, String> valueMap = new HashMap<List, String>(16);
        valueMap.put(arrays2, "2");
        List<String> arrays3 = Arrays.asList("1", "2", "3");

        System.out.println(valueMap.get(arrays));
        System.out.println(valueMap.get(arrays2));
        System.out.println(valueMap.get(arrays3));
        //System.out.println(arrays.equals(arrays2));

        FillManyKeyModel vo = new FillManyKeyModel();

        List<FillManyKeyModel> vos1 = new ArrayList<FillManyKeyModel>();
        vos1.add(vo);

        List<FillManyKeyModel> vos2 = new ArrayList<FillManyKeyModel>();
        vos2.add(vo);

        System.out.println(vos1.equals(vos2));

        valueMap.put(vos1, "2333");
        System.out.println(valueMap.get(vos1));
        System.out.println(valueMap.get(vos2));

    }


}
