package com.example.confessionwall.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "confession_likes",
       uniqueConstraints = @UniqueConstraint(name = "uk_confession_like_ip", columnNames = {"confession_id", "ip_address"}),
       indexes = @Index(name = "idx_confession_like_lookup", columnList = "confession_id, ip_address"))
public class ConfessionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "confession_id", nullable = false)
    private Long confessionId;

    @Column(name = "ip_address", length = 64, nullable = false)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ConfessionLike() {
    }

    public ConfessionLike(Long confessionId, String ipAddress) {
        this.confessionId = confessionId;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getConfessionId() {
        return confessionId;
    }

    public void setConfessionId(Long confessionId) {
        this.confessionId = confessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
