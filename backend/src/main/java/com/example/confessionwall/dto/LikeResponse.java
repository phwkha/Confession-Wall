package com.example.confessionwall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class LikeResponse implements Serializable {

    private Long id;
    private Integer likes;

    @JsonProperty("liked")
    private boolean liked;

    public LikeResponse() {
    }

    public LikeResponse(Long id, Integer likes, boolean liked) {
        this.id = id;
        this.likes = likes;
        this.liked = liked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
