package com.assetmanagement.service;

import com.assetmanagement.dto.AssignmentRequest;
import com.assetmanagement.entity.Asset;
import com.assetmanagement.entity.Assignment;
import com.assetmanagement.entity.Base;
import com.assetmanagement.exception.ApiException;
import com.assetmanagement.repository.AssignmentRepository;
import com.assetmanagement.repository.BaseRepository;
import com.assetmanagement.security.CurrentUserService;
import com.assetmanagement.support.Filters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final BaseRepository baseRepository;
    private final AssetService assetService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            BaseRepository baseRepository,
            AssetService assetService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.baseRepository = baseRepository;
        this.assetService = assetService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    public List<Assignment> list(Long baseId, String equipmentType, LocalDate startDate, LocalDate endDate) {
        Long scoped = currentUserService.scopedBaseId();
        Long effectiveBase = scoped != null ? scoped : baseId;
        List<Assignment> rows = effectiveBase == null
                ? assignmentRepository.findAllByOrderByAssignedAtDesc()
                : assignmentRepository.findByBaseIdOrderByAssignedAtDesc(effectiveBase);
        rows = Filters.byBaseAndType(
                rows,
                effectiveBase,
                equipmentType,
                row -> row.getBase().getId(),
                row -> row.getAsset().getEquipmentType());
        return Filters.byInstantRange(rows, startDate, endDate, Assignment::getAssignedAt);
    }

    @Transactional
    public Assignment create(AssignmentRequest request) {
        currentUserService.assertBaseAccess(request.baseId());
        Asset asset = assetService.get(request.assetId());
        if (!asset.getBase().getId().equals(request.baseId())) {
            throw new ApiException("Asset does not belong to the selected base");
        }
        Base base = baseRepository.findById(request.baseId()).orElseThrow(() -> new ApiException("Base not found"));
        assetService.adjustQuantity(asset, -request.quantity());
        Assignment assignment = new Assignment();
        assignment.setAsset(asset);
        assignment.setBase(base);
        assignment.setPersonnelName(request.personnelName());
        assignment.setQuantity(request.quantity());
        assignment.setAssignedAt(request.assignedAt() == null ? Instant.now() : request.assignedAt());
        assignment.setRemarks(request.remarks());
        Assignment saved = assignmentRepository.save(assignment);
        auditService.log(currentUserService.requireUser(), "CREATE_ASSIGNMENT", "Assignment", saved.getId(), "POST", "/api/assignments");
        return saved;
    }
}
