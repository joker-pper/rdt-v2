package com.devloper.joker.rdt_jpa_test.domain;

import com.devloper.joker.redundant.annotation.RdtFillType;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.rely.KeyTarget;
import com.devloper.joker.redundant.annotation.rely.RdtFieldConditionRely;
import com.devloper.joker.redundant.annotation.rely.RdtFieldRely;
import com.devloper.joker.redundant.annotation.rely.RdtRely;
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

    @RdtFieldConditionRely(property = "type", targetPropertys = "id")
    @RdtFieldCondition(target = Goods.class, property = "id")
    private String goodsId;

    @Transient
    @RdtField(target = Goods.class, property = "name")
    private String goodsName;

    /**
     * 当订单类型为2时,当goods的金额值更改后进行更新(配置fillShow后,在fillShow时会依据条件填充该列值)
     */
    @RdtFieldRely(property = "type")
    //@RdtFieldRely(property = "type", fillShow = RdtFillType.ENABLE)
    private Integer price;

    /**
     * type: 1 已完成 2: 未付款
     */
    @RdtRely(@KeyTarget(target = Goods.class, value = "2"))
    private Integer type;


}
