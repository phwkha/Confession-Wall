package com.example.confessionwall.service;

import com.example.confessionwall.dto.CommentRequest;
import com.example.confessionwall.dto.ConfessionRequest;
import com.example.confessionwall.dto.LikeResponse;
import com.example.confessionwall.model.Comment;
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

    /**
     * Toggle like for a confession by client IP.
     * If the IP has not liked the confession, like count increases by 1.
     * If the IP has already liked the confession, like count decreases by 1 (unlike).
     *
     * @param id the ID of the confession
     * @param clientIp client IP address
     * @return LikeResponse containing updated like count and current liked state
     */
    LikeResponse toggleLike(Long id, String clientIp);

    /**
     * Retrieve all comments for a confession ordered by creation timestamp ascending.
     *
     * @param confessionId the confession ID
     * @return list of comments sorted chronologically ascending
     */
    List<Comment> getComments(Long confessionId);

    /**
     * Add a comment to a confession, increment the confession's comment count,
     * and broadcast the new comment event after transaction commit.
     *
     * @param confessionId the confession ID
     * @param request the comment creation request
     * @return the saved comment entity
     */
    Comment addComment(Long confessionId, CommentRequest request);
}

