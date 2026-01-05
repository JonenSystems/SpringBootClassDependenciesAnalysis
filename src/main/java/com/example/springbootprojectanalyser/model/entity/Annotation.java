package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * アノテーションエンティティ
 */
@Entity
@Table(name = "annotations")
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassEntity classEntity;

    @Column(nullable = false, length = 500)
    private String annotationName;

    @OneToMany(mappedBy = "annotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnnotationAttribute> attributes = new ArrayList<>();

    public Annotation() {
    }

    public Annotation(Member member, String annotationName) {
        this.member = member;
        this.annotationName = annotationName;
    }

    public Annotation(ClassEntity classEntity, String annotationName) {
        this.classEntity = classEntity;
        this.annotationName = annotationName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public ClassEntity getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public void setAnnotationName(String annotationName) {
        this.annotationName = annotationName;
    }

    public List<AnnotationAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AnnotationAttribute> attributes) {
        this.attributes = attributes;
    }
}

