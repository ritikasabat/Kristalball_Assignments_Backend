package com.assetmanagement.service;

import com.assetmanagement.entity.AuditLog;
import com.assetmanagement.entity.User;
import com.assetmanagement.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String action, String entityType, Long entityId, String method, String endpoint) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setRequestMethod(method);
        log.setEndpoint(endpoint);
        auditLogRepository.save(log);
    }
}
