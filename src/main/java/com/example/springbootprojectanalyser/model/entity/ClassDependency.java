package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * クラス依存関係エンティティ
 */
@Entity
@Table(name = "class_dependencies")
public class ClassDependency {

    @Id
    @Column(nullable = false, unique = true)
    private String dependencyRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_class_id", nullable = false)
    private ClassEntity sourceClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_class_id")
    private ClassEntity targetClass;

    @Column(nullable = false)
    private String sourceClassFqn;

    @Column(nullable = false)
    private String targetIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependency_kind_code", nullable = false)
    private DependencyKindEntity dependencyKind;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    public ClassDependency() {
    }

    public ClassDependency(ClassEntity sourceClass, String sourceClassFqn, String targetIdentifier,
            DependencyKindEntity dependencyKind) {
        this.dependencyRecordId = UUID.randomUUID().toString();
        this.sourceClass = sourceClass;
        this.sourceClassFqn = sourceClassFqn;
        this.targetIdentifier = targetIdentifier;
        this.dependencyKind = dependencyKind;
        this.detectedAt = LocalDateTime.now();
    }

    public String getDependencyRecordId() {
        return dependencyRecordId;
    }

    public void setDependencyRecordId(String dependencyRecordId) {
        this.dependencyRecordId = dependencyRecordId;
    }

    public ClassEntity getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(ClassEntity sourceClass) {
        this.sourceClass = sourceClass;
    }

    public ClassEntity getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(ClassEntity targetClass) {
        this.targetClass = targetClass;
    }

    public String getSourceClassFqn() {
        return sourceClassFqn;
    }

    public void setSourceClassFqn(String sourceClassFqn) {
        this.sourceClassFqn = sourceClassFqn;
    }

    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    public void setTargetIdentifier(String targetIdentifier) {
        this.targetIdentifier = targetIdentifier;
    }

    public DependencyKindEntity getDependencyKind() {
        return dependencyKind;
    }

    public void setDependencyKind(DependencyKindEntity dependencyKind) {
        this.dependencyKind = dependencyKind;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}
