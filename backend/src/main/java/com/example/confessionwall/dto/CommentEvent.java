package com.example.confessionwall.dto;

import com.example.confessionwall.model.Comment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentEvent implements Serializable {
    private Long confessionId;
    private Comment comment;
    private Integer commentCount;

    public CommentEvent() {
    }

    public CommentEvent(Long confessionId, Comment comment) {
        this.confessionId = confessionId;
        this.comment = comment;
    }

    public CommentEvent(Long confessionId, Comment comment, Integer commentCount) {
        this.confessionId = confessionId;
        this.comment = comment;
        this.commentCount = commentCount;
    }

    public Long getConfessionId() {
        return confessionId;
    }

    public void setConfessionId(Long confessionId) {
        this.confessionId = confessionId;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Long getId() {
        return comment != null ? comment.getId() : null;
    }

    public String getContent() {
        return comment != null ? comment.getContent() : null;
    }

    public String getAuthor() {
        return comment != null ? comment.getAuthor() : null;
    }

    public LocalDateTime getCreatedAt() {
        return comment != null ? comment.getCreatedAt() : null;
    }
}
