package com.example.managelibary.service;

import com.example.managelibary.model.AuditLog;
import com.example.managelibary.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String username, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUsername(username);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}