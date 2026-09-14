package com.example.confessionwall.controller;

import com.example.confessionwall.service.RealtimeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/realtime")
@CrossOrigin(origins = "*")
public class RealtimeController {

    private final RealtimeService realtimeService;

    public RealtimeController(RealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRealtimeEvents() {
        return realtimeService.subscribe();
    }
}
