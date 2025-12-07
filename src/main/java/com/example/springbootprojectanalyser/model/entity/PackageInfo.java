package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * パッケージエンティティ
 */
@Entity
@Table(name = "packages")
public class PackageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_package_id")
    private PackageInfo parentPackage;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String simpleName;

    @OneToMany(mappedBy = "parentPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackageInfo> childPackages = new ArrayList<>();

    @OneToMany(mappedBy = "packageInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassEntity> classes = new ArrayList<>();

    public PackageInfo() {
    }

    public PackageInfo(Project project, String fullName, String simpleName) {
        this.project = project;
        this.fullName = fullName;
        this.simpleName = simpleName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public PackageInfo getParentPackage() {
        return parentPackage;
    }

    public void setParentPackage(PackageInfo parentPackage) {
        this.parentPackage = parentPackage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public List<PackageInfo> getChildPackages() {
        return childPackages;
    }

    public void setChildPackages(List<PackageInfo> childPackages) {
        this.childPackages = childPackages;
    }

    public List<ClassEntity> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassEntity> classes) {
        this.classes = classes;
    }
}

