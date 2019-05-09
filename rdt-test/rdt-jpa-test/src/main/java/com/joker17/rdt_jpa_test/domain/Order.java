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
    @RdtFieldConditionRely(property = "type", targetPropertys = "id", group = 1)
    @RdtFieldCondition(target = Goods.class, property = "id")
    private String goodsId;

    @Transient
    @RdtField(target = Goods.class, property = "name")
    private String goodsName;

    /**
     * 当订单类型为2时,当goods的金额值更改后进行更新(配置fillShow后,在fillShow时会依据条件填充该列值)
     */
    //@RdtFieldRely(property = "type")
    @RdtFieldRely(property = "type", fillShow = RdtFillType.ENABLE, details = {@RdtFieldRelyDetail(target = Goods.class, fillShowIgnoresType = "1")}, group = 1)
    private Integer price;

    /**
     * type: 1 已完成 2: 未付款 (配置allowValues在save时会忽略对应值的验证,若未找到状态值,在fillSave时将会报错)
     */
    @RdtRely(value = @KeyTarget(target = Goods.class, value = {"1", "2"}, ignoreUpdateValue = {"1"}), allowValue = "3", group = 1)
    private Integer type;


}
