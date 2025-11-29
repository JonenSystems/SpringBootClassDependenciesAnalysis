package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * クラスエンティティ
 * クラス情報を保持する
 */
@Entity
@Table(name = "classes")
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false, foreignKey = @ForeignKey(name = "fk_class_package"))
    private Package packageEntity;

    @Column(nullable = false)
    private String name;

    @Column(name = "full_qualified_name", nullable = false, unique = true)
    private String fullQualifiedName;

    @ManyToOne
    @JoinColumn(name = "class_type_id", foreignKey = @ForeignKey(name = "fk_class_class_type"))
    private ClassType classType;

    // JPA要件のためのデフォルトコンストラクタ
    protected Class() {
    }

    public Class(Package packageEntity, String name, String fullQualifiedName, ClassType classType) {
        this.packageEntity = packageEntity;
        this.name = name;
        this.fullQualifiedName = fullQualifiedName;
        this.classType = classType;
    }

    public Long getId() {
        return id;
    }

    public Package getPackageEntity() {
        return packageEntity;
    }

    public void setPackageEntity(Package packageEntity) {
        this.packageEntity = packageEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullQualifiedName() {
        return fullQualifiedName;
    }

    public void setFullQualifiedName(String fullQualifiedName) {
        this.fullQualifiedName = fullQualifiedName;
    }

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Class classEntity = (Class) o;
        return fullQualifiedName != null && fullQualifiedName.equals(classEntity.fullQualifiedName);
    }

    @Override
    public int hashCode() {
        return fullQualifiedName != null ? fullQualifiedName.hashCode() : 0;
    }
}

