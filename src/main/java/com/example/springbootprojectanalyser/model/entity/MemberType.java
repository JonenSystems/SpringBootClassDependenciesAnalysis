package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;

/**
 * メンバータイプエンティティ
 */
@Entity
@Table(name = "member_types")
public class MemberType {

    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String description;

    public MemberType() {
    }

    public MemberType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

