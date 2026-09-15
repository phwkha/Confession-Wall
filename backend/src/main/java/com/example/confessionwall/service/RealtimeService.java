package com.example.confessionwall.service;

import com.example.confessionwall.model.Comment;
import com.example.confessionwall.model.Confession;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RealtimeService {
    SseEmitter subscribe();
    void broadcastNewConfession(Confession confession);
    void broadcastLikeUpdate(Long id, Integer likes);
    void broadcastNewComment(Long confessionId, Comment comment);
    void broadcastNewComment(Long confessionId, Comment comment, Integer commentCount);
    default void broadcastNewComment(Comment comment) {
        if (comment != null) {
            broadcastNewComment(comment.getConfessionId(), comment);
        }
    }
    int getActiveSubscribersCount();
    void sendHeartbeat();
}
