package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * HTTPメソッドエンティティ
 */
@Entity
@Table(name = "http_methods")
public class HttpMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String methodName;

    public HttpMethod() {
    }

    public HttpMethod(String methodName) {
        this.methodName = methodName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpMethod that = (HttpMethod) o;
        return Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName);
    }
}

