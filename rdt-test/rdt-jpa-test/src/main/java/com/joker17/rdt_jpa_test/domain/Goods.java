package com.joker17.rdt_jpa_test.domain;

import com.joker17.redundant.annotation.fill.RdtEntityTips;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@RdtEntityTips(notFound = "商品不存在")
public class Goods {

    @Id
    private String id;
    private String name;
    private Integer price;
}
