package com.example.confessionwall.controller;

import com.example.confessionwall.dto.ConfessionRequest;
import com.example.confessionwall.model.Confession;
import com.example.confessionwall.service.ConfessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/confessions")
public class ConfessionController {

    private final ConfessionService confessionService;

    public ConfessionController(ConfessionService confessionService) {
        this.confessionService = confessionService;
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
     * Increment the like count of a confession by 1.
     *
     * @param id primary key of the confession
     * @return 200 OK with updated confession entity, or 404 if not found
     */
    @PutMapping("/{id}/like")
    public ResponseEntity<Confession> likeConfession(@PathVariable Long id) {
        Confession updated = confessionService.likeConfession(id);
        return ResponseEntity.ok(updated);
    }
}
