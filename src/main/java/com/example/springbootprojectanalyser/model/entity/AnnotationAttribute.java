package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;

/**
 * アノテーション属性エンティティ
 */
@Entity
@Table(name = "annotation_attributes")
public class AnnotationAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotation_id", nullable = false)
    private Annotation annotation;

    @Column(length = 500)
    private String attributeName;

    @Column(length = 2000)
    private String attributeValue;

    public AnnotationAttribute() {
    }

    public AnnotationAttribute(Annotation annotation, String attributeName, String attributeValue) {
        this.annotation = annotation;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }
}

