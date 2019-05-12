package com.joker17.rdt_jpa_test.domain;

import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import com.joker17.redundant.annotation.fill.RdtConditionTips;
import com.joker17.redundant.annotation.fill.RdtFieldRelyDetail;
import com.joker17.redundant.annotation.rely.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "t_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    @RdtConditionTips(nullTips = "商品id不能为空")
    /**
     * 依赖type列的标识条件
     */
    @RdtFieldConditionRelys(value = {
            @RdtFieldConditionRely(property = "type", targetPropertys = "id", group = 2),
            @RdtFieldConditionRely(property = "type", targetPropertys = "id", group = 1)
    })

    /**
     * 作为填充goodsName的标识条件
     */
    @RdtFieldCondition(target = Goods.class, property = "id")
    private String goodsId;

    @Transient
    @RdtField(target = Goods.class, property = "name")
    private String goodsName;

    /**
     * 当订单类型为未付款时,goods的金额值更改后会进行更新属性值
     * (当前为持久化列,并采用默认填充机制时,fillShow时直接使用数据本身值,fillSave时忽略数据值的填充)
     */
    @RdtFieldRely(property = "type", details = {@RdtFieldRelyDetail(target = Goods.class, fillSaveIgnoresType = "1")}, group = 1)
    private Integer price;



    /**
     * fillShow只填充未付款的金额
     * (当前为持久化列,fillShow采用ENABLE模式,fillShow时除忽略类型值外直接使用数据本身值)
     */
    @RdtFieldRely(property = "type", targetPropertys = "price", fillShow = RdtFillType.ENABLE, details = {@RdtFieldRelyDetail(target = Goods.class, fillShowIgnoresType = {"1"}, fillSaveIgnoresType = "1")}, group = 2)
    private Integer price2;


    /**
     * type: 1 已完成 2: 未付款
     * (配置allowValues在save时会忽略对应值的验证,若未找到对应状态值,在fillSave时将会报错)
     */
    @RdtRelys(
            value = {
                    /**
                     *  (配置ignoreUpdateValue将会忽略更新处于该状态值的相关属性值,当前已完成的订单的价格不应该被更新)
                     */
                    @RdtRely(value = @KeyTarget(target = Goods.class, value = {"1", "2"}, ignoreUpdateValue = {"1"}), allowValues = "3", group = 1),

                    /**
                     * 此项配置将不会进行更新相关列值
                     */
                    @RdtRely(value = @KeyTarget(target = Goods.class, value = {"1", "2"}, ignoreUpdateValue = "$value"), allowValues = "3", group = 2)
            }
    )
    private Integer type;


}
