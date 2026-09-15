package com.example.confessionwall.repository;

import com.example.confessionwall.model.Confession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ConfessionRepository extends JpaRepository<Confession, Long> {
    /**
     * Retrieve all confessions ordered by creation timestamp descending (newest
     * first).
     *
     * @return sorted list of confessions
     */
    List<Confession> findAllByOrderByCreatedAtDesc();

    /**
     * Retrieve confession by ID with a pessimistic write lock to prevent race conditions during like updates.
     *
     * @param id confession ID
     * @return optional confession
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Confession c WHERE c.id = :id")
    Optional<Confession> findByIdForUpdate(@Param("id") Long id);
}

