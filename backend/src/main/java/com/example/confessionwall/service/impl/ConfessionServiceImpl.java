package com.example.confessionwall.service.impl;

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
import com.example.confessionwall.service.ConfessionService;
import com.example.confessionwall.service.RealtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConfessionServiceImpl implements ConfessionService {

    private final ConfessionRepository confessionRepository;
    private final RealtimeService realtimeService;
    private final ConfessionLikeRepository confessionLikeRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ConfessionServiceImpl(ConfessionRepository confessionRepository,
                                 RealtimeService realtimeService,
                                 ConfessionLikeRepository confessionLikeRepository,
                                 @Autowired(required = false) CommentRepository commentRepository) {
        this.confessionRepository = confessionRepository;
        this.realtimeService = realtimeService;
        this.confessionLikeRepository = confessionLikeRepository;
        this.commentRepository = commentRepository;
    }

    public ConfessionServiceImpl(ConfessionRepository confessionRepository,
                                 RealtimeService realtimeService,
                                 ConfessionLikeRepository confessionLikeRepository) {
        this(confessionRepository, realtimeService, confessionLikeRepository, null);
    }

    public ConfessionServiceImpl(ConfessionRepository confessionRepository,
                                 RealtimeService realtimeService) {
        this(confessionRepository, realtimeService, null, null);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Confession> getAllConfessions() {
        return confessionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Confession createConfession(ConfessionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu không được để trống");
        }

        String content = request.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung lời thú tội không được để trống");
        }
        if (content.trim().length() > 1000) {
            throw new IllegalArgumentException("Nội dung lời thú tội không được vượt quá 1000 ký tự");
        }

        String author = request.getAuthor();
        if (author == null || author.trim().isEmpty()) {
            author = "Ẩn danh";
        } else {
            author = author.trim();
        }
        if (author.length() > 50) {
            throw new IllegalArgumentException("Tên tác giả không được vượt quá 50 ký tự");
        }

        Confession confession = new Confession();
        confession.setContent(content.trim());
        confession.setAuthor(author);
        confession.setLikes(0);
        confession.setCreatedAt(LocalDateTime.now());

        Confession saved = confessionRepository.save(confession);

        if (realtimeService != null) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        realtimeService.broadcastNewConfession(saved);
                    }
                });
            } else {
                realtimeService.broadcastNewConfession(saved);
            }
        }

        return saved;
    }

    @Override
    public Confession likeConfession(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID lời thú tội không được để trống");
        }

        Confession confession = confessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lời thú tội không tồn tại với ID: " + id));

        int currentLikes = (confession.getLikes() == null) ? 0 : confession.getLikes();
        confession.setLikes(currentLikes + 1);

        Confession saved = confessionRepository.save(confession);

        if (realtimeService != null) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        realtimeService.broadcastLikeUpdate(saved.getId(), saved.getLikes());
                    }
                });
            } else {
                realtimeService.broadcastLikeUpdate(saved.getId(), saved.getLikes());
            }
        }

        return saved;
    }

    @Override
    public LikeResponse toggleLike(Long id, String clientIp) {
        if (id == null) {
            throw new IllegalArgumentException("ID lời thú tội không được để trống");
        }

        String normalizedIp = (clientIp == null || clientIp.isBlank()) ? "127.0.0.1" : clientIp.trim();
        if (normalizedIp.length() > 64) {
            normalizedIp = normalizedIp.substring(0, 64);
        }

        // Pessimistic write lock on Confession to serialize updates and prevent race conditions
        Confession confession = confessionRepository.findByIdForUpdate(id)
                .or(() -> confessionRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Lời thú tội không tồn tại với ID: " + id));

        int currentLikes = (confession.getLikes() == null) ? 0 : confession.getLikes();
        boolean liked;
        int newLikes;

        if (confessionLikeRepository != null) {
            Optional<ConfessionLike> existingLike = confessionLikeRepository.findByConfessionIdAndIpAddress(id, normalizedIp);
            if (existingLike.isPresent()) {
                // Toggle OFF: Unliked
                confessionLikeRepository.deleteByConfessionIdAndIpAddress(id, normalizedIp);
                newLikes = Math.max(0, currentLikes - 1);
                liked = false;
            } else {
                // Toggle ON: Liked
                confessionLikeRepository.save(new ConfessionLike(id, normalizedIp));
                newLikes = currentLikes + 1;
                liked = true;
            }
        } else {
            newLikes = currentLikes + 1;
            liked = true;
        }

        confession.setLikes(newLikes);
        Confession saved = confessionRepository.save(confession);

        if (realtimeService != null) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        realtimeService.broadcastLikeUpdate(saved.getId(), saved.getLikes());
                    }
                });
            } else {
                realtimeService.broadcastLikeUpdate(saved.getId(), saved.getLikes());
            }
        }

        return new LikeResponse(saved.getId(), saved.getLikes(), liked);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getComments(Long confessionId) {
        if (confessionId == null) {
            throw new IllegalArgumentException("ID lời thú tội không được để trống");
        }

        if (!confessionRepository.existsById(confessionId)) {
            throw new ResourceNotFoundException("Lời thú tội không tồn tại với ID: " + confessionId);
        }

        if (commentRepository == null) {
            return Collections.emptyList();
        }

        return commentRepository.findByConfessionIdOrderByCreatedAtAsc(confessionId);
    }

    @Override
    public Comment addComment(Long confessionId, CommentRequest request) {
        if (confessionId == null) {
            throw new IllegalArgumentException("ID lời thú tội không được để trống");
        }

        // Pessimistic write lock on Confession to serialize updates and prevent race conditions with concurrent comments and likes
        Confession confession = confessionRepository.findByIdForUpdate(confessionId)
                .or(() -> confessionRepository.findById(confessionId))
                .orElseThrow(() -> new ResourceNotFoundException("Lời thú tội không tồn tại với ID: " + confessionId));

        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu không được để trống");
        }

        String content = request.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống");
        }
        if (content.trim().length() > 500) {
            throw new IllegalArgumentException("Nội dung bình luận không được vượt quá 500 ký tự");
        }

        String author = request.getAuthor();
        if (author == null || author.trim().isEmpty()) {
            author = "Ẩn danh";
        } else {
            author = author.trim();
        }
        if (author.length() > 50) {
            throw new IllegalArgumentException("Tên tác giả không được vượt quá 50 ký tự");
        }

        Comment comment = new Comment();
        comment.setConfessionId(confessionId);
        comment.setConfession(confession);
        comment.setContent(content.trim());
        comment.setAuthor(author);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = (commentRepository != null) ? commentRepository.save(comment) : comment;

        // Increment confession commentCount
        int currentCount = (confession.getCommentCount() == null) ? 0 : confession.getCommentCount();
        confession.setCommentCount(currentCount + 1);
        Confession savedConfession = confessionRepository.save(confession);
        int updatedCount = savedConfession.getCommentCount();

        // SSE Realtime broadcast after commit
        if (realtimeService != null) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        realtimeService.broadcastNewComment(confessionId, savedComment, updatedCount);
                    }
                });
            } else {
                realtimeService.broadcastNewComment(confessionId, savedComment, updatedCount);
            }
        }

        return savedComment;
    }
}

