package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * クラスエンティティ
 */
@Entity
@Table(name = "classes")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private PackageInfo packageInfo;

    @Column(nullable = false)
    private String fullQualifiedName;

    @Column(nullable = false)
    private String simpleName;

    @OneToMany(mappedBy = "sourceClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassDependency> outgoingDependencies = new ArrayList<>();

    @OneToMany(mappedBy = "targetClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassDependency> incomingDependencies = new ArrayList<>();

    public ClassEntity() {
    }

    public ClassEntity(Project project, PackageInfo packageInfo, String fullQualifiedName, String simpleName) {
        this.project = project;
        this.packageInfo = packageInfo;
        this.fullQualifiedName = fullQualifiedName;
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

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public String getFullQualifiedName() {
        return fullQualifiedName;
    }

    public void setFullQualifiedName(String fullQualifiedName) {
        this.fullQualifiedName = fullQualifiedName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public List<ClassDependency> getOutgoingDependencies() {
        return outgoingDependencies;
    }

    public void setOutgoingDependencies(List<ClassDependency> outgoingDependencies) {
        this.outgoingDependencies = outgoingDependencies;
    }

    public List<ClassDependency> getIncomingDependencies() {
        return incomingDependencies;
    }

    public void setIncomingDependencies(List<ClassDependency> incomingDependencies) {
        this.incomingDependencies = incomingDependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassEntity that = (ClassEntity) o;
        return Objects.equals(fullQualifiedName, that.fullQualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullQualifiedName);
    }
}

