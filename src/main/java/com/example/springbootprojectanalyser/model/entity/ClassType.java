package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;

/**
 * クラスタイプエンティティ
 */
@Entity
@Table(name = "class_types")
public class ClassType {

    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String description;

    public ClassType() {
    }

    public ClassType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

