package com.example.confessionwall.dto;

import java.io.Serializable;

public class LikeUpdateEvent implements Serializable {
    private Long id;
    private Integer likes;

    public LikeUpdateEvent() {}

    public LikeUpdateEvent(Long id, Integer likes) {
        this.id = id;
        this.likes = likes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
}
