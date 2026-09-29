package com.assetmanagement.repository;

import com.assetmanagement.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();

    boolean existsByUserId(Long userId);
}
