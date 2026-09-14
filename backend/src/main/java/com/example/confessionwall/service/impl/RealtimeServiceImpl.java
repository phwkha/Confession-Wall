package com.example.confessionwall.service.impl;

import com.example.confessionwall.dto.LikeUpdateEvent;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.service.RealtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealtimeServiceImpl implements RealtimeService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeServiceImpl.class);
    private static final Long SSE_TIMEOUT = 86_400_000L; // 24 hours

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Override
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE emitter completed");
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE emitter timed out");
            emitters.remove(emitter);
        });

        emitter.onError(e -> {
            log.debug("SSE emitter encountered error: {}", e.getMessage());
            emitters.remove(emitter);
        });

        try {
            synchronized (emitter) {
                emitter.send(SseEmitter.event()
                        .name("CONNECTED")
                        .data("{\"status\":\"CONNECTED\",\"message\":\"Realtime stream connected\"}"));
            }
        } catch (IOException e) {
            log.warn("Failed to send initial CONNECTED event, removing emitter: {}", e.getMessage());
            emitters.remove(emitter);
        }

        return emitter;
    }

    @Override
    public void broadcastNewConfession(Confession confession) {
        if (confession == null || emitters.isEmpty()) return;
        CompletableFuture.runAsync(() -> {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : emitters) {
                try {
                    synchronized (emitter) {
                        emitter.send(SseEmitter.event()
                                .name("NEW_CONFESSION")
                                .data(confession));
                    }
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }
            if (!deadEmitters.isEmpty()) {
                emitters.removeAll(deadEmitters);
            }
        });
    }

    @Override
    public void broadcastLikeUpdate(Long id, Integer likes) {
        if (id == null || likes == null || emitters.isEmpty()) return;
        LikeUpdateEvent event = new LikeUpdateEvent(id, likes);
        CompletableFuture.runAsync(() -> {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for (SseEmitter emitter : emitters) {
                try {
                    synchronized (emitter) {
                        emitter.send(SseEmitter.event()
                                .name("LIKE_UPDATE")
                                .data(event));
                    }
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }
            if (!deadEmitters.isEmpty()) {
                emitters.removeAll(deadEmitters);
            }
        });
    }

    @Override
    public int getActiveSubscribersCount() {
        return emitters.size();
    }

    @Scheduled(fixedRate = 25000)
    @Override
    public void sendHeartbeat() {
        if (emitters.isEmpty()) return;
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                synchronized (emitter) {
                    emitter.send(SseEmitter.event().comment("ping"));
                }
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }
}
