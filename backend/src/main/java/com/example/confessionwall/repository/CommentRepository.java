package com.example.confessionwall.repository;

import com.example.confessionwall.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Retrieve all comments for a given confession ordered by creation timestamp ascending (oldest first).
     *
     * @param confessionId the confession ID
     * @return list of comments sorted chronologically ascending
     */
    List<Comment> findByConfessionIdOrderByCreatedAtAsc(Long confessionId);

    /**
     * Count comments associated with a confession.
     *
     * @param confessionId the confession ID
     * @return total comments count
     */
    long countByConfessionId(Long confessionId);

    /**
     * Delete all comments associated with a confession.
     *
     * @param confessionId the confession ID
     */
    void deleteByConfessionId(Long confessionId);
}
