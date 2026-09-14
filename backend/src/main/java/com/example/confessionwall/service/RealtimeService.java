package com.example.confessionwall.service;

import com.example.confessionwall.model.Confession;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RealtimeService {
    SseEmitter subscribe();
    void broadcastNewConfession(Confession confession);
    void broadcastLikeUpdate(Long id, Integer likes);
    int getActiveSubscribersCount();
    void sendHeartbeat();
}
