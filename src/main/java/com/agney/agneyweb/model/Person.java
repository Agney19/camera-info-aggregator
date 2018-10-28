package com.agney.agneyweb.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Entity;

@Getter
@Setter
@ToString
@Entity
@Table(name = "person")
public class Person extends AbstractModel {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;
}
