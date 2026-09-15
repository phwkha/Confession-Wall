package com.example.confessionwall.service;

import com.example.confessionwall.model.Comment;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.service.impl.RealtimeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RealtimeServiceTest {

    private RealtimeServiceImpl realtimeService;

    @BeforeEach
    void setUp() {
        realtimeService = new RealtimeServiceImpl();
    }

    @Test
    @DisplayName("subscribe should register emitter and increment active count")
    void subscribe_shouldAddEmitter() {
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(0);
        SseEmitter emitter = realtimeService.subscribe();
        assertThat(emitter).isNotNull();
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("broadcastNewConfession with null payload is safe")
    void broadcastNewConfession_null_safe() {
        realtimeService.broadcastNewConfession(null);
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("broadcastLikeUpdate with null values is safe")
    void broadcastLikeUpdate_null_safe() {
        realtimeService.broadcastLikeUpdate(null, 10);
        realtimeService.broadcastLikeUpdate(1L, null);
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("broadcastNewComment with null values is safe")
    void broadcastNewComment_null_safe() {
        realtimeService.broadcastNewComment(null, new Comment(1L, "Test", "Author"));
        realtimeService.broadcastNewComment(1L, null);
        realtimeService.broadcastNewComment(null);
        realtimeService.broadcastNewComment(null, new Comment(1L, "Test", "Author"), 1);
        realtimeService.broadcastNewComment(1L, null, 1);
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("sendHeartbeat on empty list is safe")
    void sendHeartbeat_empty_safe() {
        realtimeService.sendHeartbeat();
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("broadcastNewConfession to active emitter succeeds")
    void broadcastNewConfession_activeSubscriber() throws Exception {
        realtimeService.subscribe();
        Confession confession = new Confession(1L, "Content", "Author", 0, LocalDateTime.now());
        realtimeService.broadcastNewConfession(confession);
        Thread.sleep(100);
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("broadcastLikeUpdate to active emitter succeeds")
    void broadcastLikeUpdate_activeSubscriber() throws Exception {
        realtimeService.subscribe();
        realtimeService.broadcastLikeUpdate(1L, 5);
        Thread.sleep(100);
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("broadcastNewComment to active emitter succeeds")
    void broadcastNewComment_activeSubscriber() throws Exception {
        realtimeService.subscribe();
        Comment comment = new Comment(10L, 1L, "Bình luận mới", "Lan", LocalDateTime.now());
        realtimeService.broadcastNewComment(1L, comment, 3);
        Thread.sleep(100);
        assertThat(realtimeService.getActiveSubscribersCount()).isEqualTo(1);
    }
}
