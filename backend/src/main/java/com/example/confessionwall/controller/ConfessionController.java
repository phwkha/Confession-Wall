package com.example.confessionwall.controller;

import com.example.confessionwall.dto.CommentRequest;
import com.example.confessionwall.dto.ConfessionRequest;
import com.example.confessionwall.dto.LikeResponse;
import com.example.confessionwall.model.Comment;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.ratelimit.ClientIpResolver;
import com.example.confessionwall.service.ConfessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/confessions")
public class ConfessionController {

    private final ConfessionService confessionService;
    private final ClientIpResolver clientIpResolver;

    @Autowired
    public ConfessionController(ConfessionService confessionService,
                                @Autowired(required = false) ClientIpResolver clientIpResolver) {
        this.confessionService = confessionService;
        this.clientIpResolver = clientIpResolver != null ? clientIpResolver : new ClientIpResolver();
    }

    public ConfessionController(ConfessionService confessionService) {
        this(confessionService, new ClientIpResolver());
    }

    /**
     * Retrieve all confessions sorted by creation time descending (newest first).
     *
     * @return 200 OK with list of confessions
     */
    @GetMapping
    public ResponseEntity<List<Confession>> getAllConfessions() {
        List<Confession> confessions = confessionService.getAllConfessions();
        return ResponseEntity.ok(confessions);
    }

    /**
     * Create and persist a new confession.
     *
     * @param request JSON payload containing content and optional author
     * @return 201 Created with persisted confession entity
     */
    @PostMapping
    public ResponseEntity<Confession> createConfession(@Valid @RequestBody ConfessionRequest request) {
        Confession created = confessionService.createConfession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Toggle like count of a confession by client IP.
     *
     * @param id primary key of the confession
     * @param request HTTP request to resolve client IP
     * @return 200 OK with LikeResponse containing updated likes count and liked status
     */
    @PutMapping("/{id}/like")
    public ResponseEntity<LikeResponse> likeConfession(@PathVariable Long id, HttpServletRequest request) {
        String clientIp = clientIpResolver.resolveClientIp(request);
        LikeResponse response = confessionService.toggleLike(id, clientIp);
        if (response == null) {
            Confession fallback = confessionService.likeConfession(id);
            response = new LikeResponse(fallback.getId(), fallback.getLikes(), true);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve all comments for a confession ordered by creation timestamp ascending.
     *
     * @param id primary key of the confession
     * @return 200 OK with list of comments
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        List<Comment> comments = confessionService.getComments(id);
        return ResponseEntity.ok(comments);
    }

    /**
     * Create and persist a new comment for a confession.
     *
     * @param id primary key of the confession
     * @param request JSON payload containing content and optional author
     * @return 201 Created with persisted comment entity
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        Comment comment = confessionService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}

