package com.example.confessionwall.service;

import com.example.confessionwall.dto.ConfessionRequest;
import com.example.confessionwall.model.Confession;

import java.util.List;

public interface ConfessionService {

    /**
     * Retrieve all confessions sorted by creation time descending (newest first).
     *
     * @return list of confessions
     */
    List<Confession> getAllConfessions();

    /**
     * Create and persist a new confession after validating content and normalizing author.
     *
     * @param request the confession creation request
     * @return the saved confession entity
     */
    Confession createConfession(ConfessionRequest request);

    /**
     * Increment the like counter of a confession by 1.
     *
     * @param id the ID of the confession to like
     * @return the updated confession entity
     */
    Confession likeConfession(Long id);
}
