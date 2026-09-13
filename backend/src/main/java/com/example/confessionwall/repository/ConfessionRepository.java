package com.example.confessionwall.repository;

import com.example.confessionwall.model.Confession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfessionRepository extends JpaRepository<Confession, Long> {
    /**
     * Retrieve all confessions ordered by creation timestamp descending (newest
     * first).
     *
     * @return sorted list of confessions
     */
    List<Confession> findAllByOrderByCreatedAtDesc();
}
