package com.example.confessionwall.dto;

import jakarta.validation.constraints.NotBlank;

public class ConfessionRequest {

    @NotBlank(message = "Nội dung lời thú tội không được để trống")
    private String content;

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
