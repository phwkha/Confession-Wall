package com.example.confessionwall.repository;

import com.example.confessionwall.model.ConfessionLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfessionLikeRepository extends JpaRepository<ConfessionLike, Long> {

    Optional<ConfessionLike> findByConfessionIdAndIpAddress(Long confessionId, String ipAddress);

    boolean existsByConfessionIdAndIpAddress(Long confessionId, String ipAddress);

    void deleteByConfessionIdAndIpAddress(Long confessionId, String ipAddress);

    long countByConfessionId(Long confessionId);
}
