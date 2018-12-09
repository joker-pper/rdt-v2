package com.joker17.rdt_jpa_test.domain;

import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import com.joker17.redundant.annotation.rely.KeyTarget;
import com.joker17.redundant.annotation.rely.RdtFieldConditionRely;
import com.joker17.redundant.annotation.rely.RdtFieldRely;
import com.joker17.redundant.annotation.rely.RdtRely;
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
     * type: 1 已完成 2: 未付款 (配置allowValues在save时会忽略对应值的验证,若未找到状态值,保存时将会报错)
     */
    @RdtRely(value = @KeyTarget(target = Goods.class, value = "2"), allowValues = "1")
    private Integer type;


}
