package com.joker17.rdt_jpa_test.domain;

import com.joker17.redundant.annotation.field.RdtLogicalField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Date createTime;

    @RdtLogicalField
    private int status;
}
