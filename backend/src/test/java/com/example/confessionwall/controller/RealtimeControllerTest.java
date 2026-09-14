package com.example.confessionwall.controller;

import com.example.confessionwall.exception.GlobalExceptionHandler;
import com.example.confessionwall.service.RealtimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RealtimeController.class)
@Import(GlobalExceptionHandler.class)
class RealtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RealtimeService realtimeService;

    @Test
    @DisplayName("GET /api/realtime should start async processing and return SseEmitter")
    void streamRealtimeEvents_shouldReturnSseStream() throws Exception {
        SseEmitter emitter = new SseEmitter();
        when(realtimeService.subscribe()).thenReturn(emitter);

        mockMvc.perform(get("/api/realtime"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }
}
