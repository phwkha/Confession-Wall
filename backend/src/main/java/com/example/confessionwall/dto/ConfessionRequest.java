package com.example.confessionwall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ConfessionRequest {

    @NotBlank(message = "Nội dung lời thú tội không được để trống")
    @Size(max = 1000, message = "Nội dung lời thú tội không được vượt quá 1000 ký tự")
    private String content;

    @Size(max = 50, message = "Tên tác giả không được vượt quá 50 ký tự")
    private String author;

    public ConfessionRequest() {
    }

    public ConfessionRequest(String content, String author) {
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
