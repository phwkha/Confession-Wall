package com.example.confessionwall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(max = 500, message = "Nội dung bình luận không được vượt quá 500 ký tự")
    private String content;

    @Size(max = 50, message = "Tên tác giả không được vượt quá 50 ký tự")
    private String author;

    public CommentRequest() {
    }

    public CommentRequest(String content, String author) {
        this.content = content;
        this.author = author;
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
}
