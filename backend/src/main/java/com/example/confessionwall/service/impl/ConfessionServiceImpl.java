package com.example.confessionwall.service.impl;

import com.example.confessionwall.dto.ConfessionRequest;
import com.example.confessionwall.exception.ResourceNotFoundException;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.repository.ConfessionRepository;
import com.example.confessionwall.service.ConfessionService;
import com.example.confessionwall.service.RealtimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ConfessionServiceImpl implements ConfessionService {

    private final ConfessionRepository confessionRepository;
    private final RealtimeService realtimeService;

    public ConfessionServiceImpl(ConfessionRepository confessionRepository,
                                 RealtimeService realtimeService) {
        this.confessionRepository = confessionRepository;
        this.realtimeService = realtimeService;
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
}
