package com.example.confessionwall.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "comments",
    indexes = {
        @Index(name = "idx_comments_confession_id", columnList = "confession_id")
    }
)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "confession_id", nullable = false)
    private Long confessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "confession_id",
        nullable = false,
        insertable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_comments_confession_id")
    )
    @JsonIgnore
    private Confession confession;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(max = 500, message = "Nội dung bình luận không được vượt quá 500 ký tự")
    @Column(name = "content", length = 500, nullable = false)
    private String content;

    @Size(max = 50, message = "Tên tác giả không được vượt quá 50 ký tự")
    @Column(name = "author", length = 50, nullable = false)
    private String author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Comment() {
    }

    public Comment(Long confessionId, String content, String author) {
        this.confessionId = confessionId;
        this.content = content;
        this.author = (author == null || author.trim().isEmpty()) ? "Ẩn danh" : author.trim();
        this.createdAt = LocalDateTime.now();
    }

    public Comment(Long id, Long confessionId, String content, String author, LocalDateTime createdAt) {
        this.id = id;
        this.confessionId = confessionId;
        this.content = content;
        this.author = (author == null || author.trim().isEmpty()) ? "Ẩn danh" : author.trim();
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConfessionId() {
        return confessionId;
    }

    public void setConfessionId(Long confessionId) {
        this.confessionId = confessionId;
    }

    public Confession getConfession() {
        return confession;
    }

    public void setConfession(Confession confession) {
        this.confession = confession;
        if (confession != null) {
            this.confessionId = confession.getId();
        }
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
        Comment comment = (Comment) o;
        return id != null && Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", confessionId=" + confessionId +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
