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
 * パッケージエンティティ
 * パッケージ情報を保持する（階層構造対応）
 */
@Entity
@Table(name = "packages")
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_package_project"))
    private Project project;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_package_id", foreignKey = @ForeignKey(name = "fk_package_parent"))
    private Package parentPackage;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    // JPA要件のためのデフォルトコンストラクタ
    protected Package() {
    }

    public Package(Project project, String name, Package parentPackage, String fullName) {
        this.project = project;
        this.name = name;
        this.parentPackage = parentPackage;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Package getParentPackage() {
        return parentPackage;
    }

    public void setParentPackage(Package parentPackage) {
        this.parentPackage = parentPackage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

