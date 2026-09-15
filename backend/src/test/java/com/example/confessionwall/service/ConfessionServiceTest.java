package com.example.confessionwall.service;

import com.example.confessionwall.dto.CommentRequest;
import com.example.confessionwall.dto.ConfessionRequest;
import com.example.confessionwall.dto.LikeResponse;
import com.example.confessionwall.exception.ResourceNotFoundException;
import com.example.confessionwall.model.Comment;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.model.ConfessionLike;
import com.example.confessionwall.repository.CommentRepository;
import com.example.confessionwall.repository.ConfessionLikeRepository;
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

    @Mock
    private ConfessionLikeRepository confessionLikeRepository;

    @Mock
    private CommentRepository commentRepository;

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

    @Test
    @DisplayName("toggleLike when IP has not liked yet should increment likes and return liked=true")
    void toggleLike_whenNotLikedYet_shouldIncrementLikes() {
        when(confessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleConfession1));
        when(confessionLikeRepository.findByConfessionIdAndIpAddress(1L, "192.168.1.100")).thenReturn(Optional.empty());
        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LikeResponse response = confessionService.toggleLike(1L, "192.168.1.100");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLikes()).isEqualTo(4); // was 3 -> 4
        assertThat(response.isLiked()).isTrue();

        verify(confessionLikeRepository, times(1)).save(any(ConfessionLike.class));
        verify(confessionLikeRepository, never()).deleteByConfessionIdAndIpAddress(any(), any());
        verify(confessionRepository, times(1)).save(sampleConfession1);
        verify(realtimeService, times(1)).broadcastLikeUpdate(1L, 4);
    }

    @Test
    @DisplayName("toggleLike when IP already liked should decrement likes and return liked=false")
    void toggleLike_whenAlreadyLiked_shouldDecrementLikes() {
        ConfessionLike existingLike = new ConfessionLike(1L, "192.168.1.100");
        when(confessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleConfession1));
        when(confessionLikeRepository.findByConfessionIdAndIpAddress(1L, "192.168.1.100")).thenReturn(Optional.of(existingLike));
        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LikeResponse response = confessionService.toggleLike(1L, "192.168.1.100");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLikes()).isEqualTo(2); // was 3 -> 2
        assertThat(response.isLiked()).isFalse();

        verify(confessionLikeRepository, times(1)).deleteByConfessionIdAndIpAddress(1L, "192.168.1.100");
        verify(confessionLikeRepository, never()).save(any(ConfessionLike.class));
        verify(confessionRepository, times(1)).save(sampleConfession1);
        verify(realtimeService, times(1)).broadcastLikeUpdate(1L, 2);
    }

    @Test
    @DisplayName("toggleLike when already liked and likes count is zero should not go below zero")
    void toggleLike_whenLikesCountZero_shouldNotGoBelowZero() {
        Confession zeroLikesConfession = new Confession(2L, "Zero likes", "Ẩn danh", 0, LocalDateTime.now());
        ConfessionLike existingLike = new ConfessionLike(2L, "192.168.1.100");
        when(confessionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(zeroLikesConfession));
        when(confessionLikeRepository.findByConfessionIdAndIpAddress(2L, "192.168.1.100")).thenReturn(Optional.of(existingLike));
        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LikeResponse response = confessionService.toggleLike(2L, "192.168.1.100");

        assertThat(response).isNotNull();
        assertThat(response.getLikes()).isEqualTo(0);
        assertThat(response.isLiked()).isFalse();
    }

    @Test
    @DisplayName("getComments should return comments ordered chronologically ascending")
    void getComments_existingConfession_shouldReturnListOrderedAscending() {
        Comment c1 = new Comment(1L, 1L, "Bình luận 1", "An", LocalDateTime.now().minusMinutes(5));
        Comment c2 = new Comment(2L, 1L, "Bình luận 2", "Bình", LocalDateTime.now());

        when(confessionRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByConfessionIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList(c1, c2));

        List<Comment> comments = confessionService.getComments(1L);

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getContent()).isEqualTo("Bình luận 1");
        assertThat(comments.get(1).getContent()).isEqualTo("Bình luận 2");
        verify(confessionRepository).existsById(1L);
        verify(commentRepository).findByConfessionIdOrderByCreatedAtAsc(1L);
    }

    @Test
    @DisplayName("getComments for non-existent confession should throw ResourceNotFoundException")
    void getComments_nonExistentConfession_shouldThrowResourceNotFoundException() {
        when(confessionRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> confessionService.getComments(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lời thú tội không tồn tại với ID: 999");

        verify(commentRepository, never()).findByConfessionIdOrderByCreatedAtAsc(any());
    }

    @Test
    @DisplayName("getComments with null ID should throw IllegalArgumentException")
    void getComments_nullId_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> confessionService.getComments(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID lời thú tội không được để trống");
    }

    @Test
    @DisplayName("addComment should save comment, increment confession commentCount, and broadcast realtime event")
    void addComment_validRequest_shouldSaveCommentAndIncrementCountAndBroadcast() {
        when(confessionRepository.findById(1L)).thenReturn(Optional.of(sampleConfession1));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(101L);
            return c;
        });
        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentRequest request = new CommentRequest("Thật xúc động!", "Người bạn");
        Comment savedComment = confessionService.addComment(1L, request);

        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getId()).isEqualTo(101L);
        assertThat(savedComment.getConfessionId()).isEqualTo(1L);
        assertThat(savedComment.getContent()).isEqualTo("Thật xúc động!");
        assertThat(savedComment.getAuthor()).isEqualTo("Người bạn");
        assertThat(savedComment.getCreatedAt()).isNotNull();

        assertThat(sampleConfession1.getCommentCount()).isEqualTo(1);
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(confessionRepository, times(1)).save(sampleConfession1);
        verify(realtimeService, times(1)).broadcastNewComment(eq(1L), eq(savedComment), eq(1));
    }

    @Test
    @DisplayName("addComment should prioritize findByIdForUpdate for pessimistic locking")
    void addComment_whenFindByIdForUpdatePresent_shouldUsePessimisticLock() {
        when(confessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleConfession1));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentRequest request = new CommentRequest("Khóa bi quan hoạt động tốt", "QA");
        Comment savedComment = confessionService.addComment(1L, request);

        assertThat(savedComment).isNotNull();
        verify(confessionRepository).findByIdForUpdate(1L);
        verify(realtimeService).broadcastNewComment(eq(1L), any(Comment.class), eq(1));
    }

    @Test
    @DisplayName("addComment with null or blank author should default to 'Ẩn danh'")
    void addComment_blankAuthor_shouldDefaultToAnonymous() {
        when(confessionRepository.findById(1L)).thenReturn(Optional.of(sampleConfession1));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(confessionRepository.save(any(Confession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentRequest requestNullAuthor = new CommentRequest("Nội dung hay", null);
        Comment c1 = confessionService.addComment(1L, requestNullAuthor);
        assertThat(c1.getAuthor()).isEqualTo("Ẩn danh");

        CommentRequest requestBlankAuthor = new CommentRequest("Nội dung hay 2", "   ");
        Comment c2 = confessionService.addComment(1L, requestBlankAuthor);
        assertThat(c2.getAuthor()).isEqualTo("Ẩn danh");
    }

    @Test
    @DisplayName("addComment with blank or null content should throw IllegalArgumentException")
    void addComment_blankOrNullContent_shouldThrowIllegalArgumentException() {
        when(confessionRepository.findById(1L)).thenReturn(Optional.of(sampleConfession1));

        assertThatThrownBy(() -> confessionService.addComment(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Yêu cầu không được để trống");

        assertThatThrownBy(() -> confessionService.addComment(1L, new CommentRequest(null, "Tác giả")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nội dung bình luận không được để trống");

        assertThatThrownBy(() -> confessionService.addComment(1L, new CommentRequest("   ", "Tác giả")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nội dung bình luận không được để trống");

        assertThatThrownBy(() -> confessionService.addComment(1L, new CommentRequest("C".repeat(501), "Tác giả")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nội dung bình luận không được vượt quá 500 ký tự");

        assertThatThrownBy(() -> confessionService.addComment(1L, new CommentRequest("Hợp lệ", "D".repeat(51))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tên tác giả không được vượt quá 50 ký tự");

        verify(commentRepository, never()).save(any());
        verify(realtimeService, never()).broadcastNewComment(any(), any());
    }

    @Test
    @DisplayName("addComment with non-existent confession should throw ResourceNotFoundException")
    void addComment_nonExistentConfession_shouldThrowResourceNotFoundException() {
        when(confessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> confessionService.addComment(999L, new CommentRequest("Hợp lệ", "Tác giả")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lời thú tội không tồn tại với ID: 999");

        verify(commentRepository, never()).save(any());
        verify(realtimeService, never()).broadcastNewComment(any(), any());
    }

    @Test
    @DisplayName("addComment with null ID should throw IllegalArgumentException")
    void addComment_nullId_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> confessionService.addComment(null, new CommentRequest("Hợp lệ", "Tác giả")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID lời thú tội không được để trống");
    }
}

