package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 依存タイプエンティティ
 * 依存関係の種類を保持する（Enum相当）
 */
@Entity
@Table(name = "dependency_kinds")
public class DependencyKind {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    // JPA要件のためのデフォルトコンストラクタ
    protected DependencyKind() {
    }

    public DependencyKind(String code, String name, String description, String category) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

