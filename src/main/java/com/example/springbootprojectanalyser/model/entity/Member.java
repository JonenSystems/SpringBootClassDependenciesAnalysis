package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * メンバーエンティティ
 */
@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_type_code", nullable = false)
    private MemberType memberType;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 1000)
    private String returnType;

    @Column(length = 20)
    private String visibility;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Annotation> annotations = new ArrayList<>();

    public Member() {
    }

    public Member(ClassEntity classEntity, MemberType memberType, String name, String returnType, String visibility) {
        this.classEntity = classEntity;
        this.memberType = memberType;
        this.name = name;
        this.returnType = returnType;
        this.visibility = visibility;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClassEntity getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }
}

