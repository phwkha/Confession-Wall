package com.example.confessionwall.service;

import com.example.confessionwall.dto.ConfessionRequest;
import com.example.confessionwall.exception.ResourceNotFoundException;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.repository.ConfessionRepository;
import com.example.confessionwall.service.impl.ConfessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfessionServiceTest {

    @Mock
    private ConfessionRepository confessionRepository;

    @Mock
    private RealtimeService realtimeService;

    @InjectMocks
    private ConfessionServiceImpl confessionService;

    private Confession sampleConfession1;
    private Confession sampleConfession2;

    @BeforeEach
    void setUp() {
        sampleConfession1 = new Confession(1L, "Lời thú tội 1", "Nam", 3, LocalDateTime.now().minusHours(1));
        sampleConfession2 = new Confession(2L, "Lời thú tội 2", "Ẩn danh", 0, LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllConfessions should return all confessions ordered by createdAt descending")
    void getAllConfessions_shouldReturnListOrderedDescending() {
        when(confessionRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(sampleConfession2, sampleConfession1));

        List<Confession> result = confessionService.getAllConfessions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
        verify(confessionRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("createConfession should save confession with provided author and initial 0 likes")
    void createConfession_withCustomAuthor_shouldSaveSuccessfully() {
        ConfessionRequest request = new ConfessionRequest("Hôm nay trời đẹp quá", "Minh");

        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> {
            Confession c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        Confession created = confessionService.createConfession(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getContent()).isEqualTo("Hôm nay trời đẹp quá");
        assertThat(created.getAuthor()).isEqualTo("Minh");
        assertThat(created.getLikes()).isEqualTo(0);
        assertThat(created.getCreatedAt()).isNotNull();

        ArgumentCaptor<Confession> captor = ArgumentCaptor.forClass(Confession.class);
        verify(confessionRepository).save(captor.capture());
        Confession captured = captor.getValue();
        assertThat(captured.getAuthor()).isEqualTo("Minh");
        assertThat(captured.getContent()).isEqualTo("Hôm nay trời đẹp quá");

        verify(realtimeService, times(1)).broadcastNewConfession(created);
    }

    @Test
    @DisplayName("createConfession with null or blank author should default to 'Ẩn danh'")
    void createConfession_withBlankAuthor_shouldDefaultToAnonymous() {
        ConfessionRequest requestNullAuthor = new ConfessionRequest("Tớ thích bạn", null);
        ConfessionRequest requestEmptyAuthor = new ConfessionRequest("Tớ thích bạn", "   ");

        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Confession created1 = confessionService.createConfession(requestNullAuthor);
        assertThat(created1.getAuthor()).isEqualTo("Ẩn danh");

        Confession created2 = confessionService.createConfession(requestEmptyAuthor);
        assertThat(created2.getAuthor()).isEqualTo("Ẩn danh");
    }

    @Test
    @DisplayName("createConfession with null or blank content should throw IllegalArgumentException")
    void createConfession_withBlankContent_shouldThrowException() {
        ConfessionRequest requestNullContent = new ConfessionRequest(null, "Nam");
        ConfessionRequest requestBlankContent = new ConfessionRequest("   ", "Nam");

        assertThatThrownBy(() -> confessionService.createConfession(requestNullContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nội dung lời thú tội không được để trống");

        assertThatThrownBy(() -> confessionService.createConfession(requestBlankContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nội dung lời thú tội không được để trống");

        assertThatThrownBy(() -> confessionService.createConfession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Yêu cầu không được để trống");

        ConfessionRequest requestOversizedContent = new ConfessionRequest("A".repeat(1001), "Nam");
        assertThatThrownBy(() -> confessionService.createConfession(requestOversizedContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nội dung lời thú tội không được vượt quá 1000 ký tự");

        ConfessionRequest requestOversizedAuthor = new ConfessionRequest("Nội dung hợp lệ", "B".repeat(51));
        assertThatThrownBy(() -> confessionService.createConfession(requestOversizedAuthor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tên tác giả không được vượt quá 50 ký tự");

        verify(confessionRepository, never()).save(any());
        verify(realtimeService, never()).broadcastNewConfession(any());
    }

    @Test
    @DisplayName("likeConfession should increment like count by 1 and return updated entity")
    void likeConfession_withExistingId_shouldIncrementLikes() {
        when(confessionRepository.findById(1L)).thenReturn(Optional.of(sampleConfession1));
        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Confession updated = confessionService.likeConfession(1L);

        assertThat(updated).isNotNull();
        assertThat(updated.getLikes()).isEqualTo(4); // was 3, incremented to 4
        verify(confessionRepository, times(1)).findById(1L);
        verify(confessionRepository, times(1)).save(sampleConfession1);
        verify(realtimeService, times(1)).broadcastLikeUpdate(1L, 4);
    }

    @Test
    @DisplayName("likeConfession with non-existent ID should throw ResourceNotFoundException")
    void likeConfession_withNonExistentId_shouldThrowResourceNotFoundException() {
        when(confessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> confessionService.likeConfession(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lời thú tội không tồn tại với ID: 999");

        verify(confessionRepository, times(1)).findById(999L);
        verify(confessionRepository, never()).save(any());
        verify(realtimeService, never()).broadcastLikeUpdate(any(), any());
    }

    @Test
    @DisplayName("likeConfession with null ID should throw IllegalArgumentException")
    void likeConfession_withNullId_shouldThrowException() {
        assertThatThrownBy(() -> confessionService.likeConfession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID lời thú tội không được để trống");

        verify(confessionRepository, never()).findById(any());
        verify(realtimeService, never()).broadcastLikeUpdate(any(), any());
    }
}
