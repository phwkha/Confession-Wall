package com.example.confessionwall.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "confessions")
public class Confession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nội dung lời thú tội không được để trống")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Confession() {
    }

    public Confession(String content, String author) {
        this.content = content;
        this.author = (author == null || author.trim().isEmpty()) ? "Ẩn danh" : author.trim();
        this.likes = 0;
        this.commentCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Confession(Long id, String content, String author, Integer likes, LocalDateTime createdAt) {
        this(id, content, author, likes, 0, createdAt);
    }

    public Confession(Long id, String content, String author, Integer likes, Integer commentCount, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.author = (author == null || author.trim().isEmpty()) ? "Ẩn danh" : author.trim();
        this.likes = (likes == null) ? 0 : likes;
        this.commentCount = (commentCount == null) ? 0 : commentCount;
        this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.author == null || this.author.trim().isEmpty()) {
            this.author = "Ẩn danh";
        }
        if (this.likes == null) {
            this.likes = 0;
        }
        if (this.commentCount == null) {
            this.commentCount = 0;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getCommentCount() {
        return commentCount != null ? commentCount : 0;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount != null ? commentCount : 0;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Confession that = (Confession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Confession{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", likes=" + likes +
                ", commentCount=" + commentCount +
                ", createdAt=" + createdAt +
                '}';
    }
}
