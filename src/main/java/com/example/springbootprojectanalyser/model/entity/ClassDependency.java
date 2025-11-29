package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 依存関係エンティティ
 * クラス間の依存関係を保持する（主要テーブル）
 */
@Entity
@Table(name = "class_dependencies")
public class ClassDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dependency_record_id", nullable = false)
    private UUID dependencyRecordId;

    @Column(name = "source_class_fqn", nullable = false)
    private String sourceClassFqn;

    @Column(name = "target_identifier", nullable = false)
    private String targetIdentifier;

    @Column(name = "dependency_kind_code", nullable = false)
    private String dependencyKindCode;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "analysis_batch_id")
    private String analysisBatchId;

    // JPA要件のためのデフォルトコンストラクタ
    protected ClassDependency() {
    }

    public ClassDependency(String sourceClassFqn, String targetIdentifier, String dependencyKindCode, String analysisBatchId) {
        this.sourceClassFqn = sourceClassFqn;
        this.targetIdentifier = targetIdentifier;
        this.dependencyKindCode = dependencyKindCode;
        this.analysisBatchId = analysisBatchId;
        this.detectedAt = LocalDateTime.now();
    }

    public UUID getDependencyRecordId() {
        return dependencyRecordId;
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

    public String getDependencyKindCode() {
        return dependencyKindCode;
    }

    public void setDependencyKindCode(String dependencyKindCode) {
        this.dependencyKindCode = dependencyKindCode;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getAnalysisBatchId() {
        return analysisBatchId;
    }

    public void setAnalysisBatchId(String analysisBatchId) {
        this.analysisBatchId = analysisBatchId;
    }
}

