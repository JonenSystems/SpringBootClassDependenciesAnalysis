package com.example.springbootprojectanalyser.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * エンドポイントエンティティ
 */
@Entity
@Table(name = "endpoints")
public class Endpoint {

    @Id
    @Column(nullable = false, unique = true, length = 36)
    private String endpointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Column(nullable = false, length = 1000)
    private String uri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "http_method_id", nullable = false)
    private HttpMethod httpMethod;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    public Endpoint() {
    }

    public Endpoint(ClassEntity classEntity, String uri, HttpMethod httpMethod) {
        this.endpointId = UUID.randomUUID().toString();
        this.classEntity = classEntity;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.detectedAt = LocalDateTime.now();
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public ClassEntity getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}

