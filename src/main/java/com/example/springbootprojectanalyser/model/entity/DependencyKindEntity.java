package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;

/**
 * 依存タイプエンティティ
 */
@Entity
@Table(name = "dependency_kinds")
public class DependencyKindEntity {

    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String description;

    public DependencyKindEntity() {
    }

    public DependencyKindEntity(String code, String description) {
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

