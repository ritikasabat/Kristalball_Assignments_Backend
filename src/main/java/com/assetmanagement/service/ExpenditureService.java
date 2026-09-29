package com.assetmanagement.service;

import com.assetmanagement.dto.ExpenditureRequest;
import com.assetmanagement.entity.Asset;
import com.assetmanagement.entity.Base;
import com.assetmanagement.entity.Expenditure;
import com.assetmanagement.exception.ApiException;
import com.assetmanagement.repository.BaseRepository;
import com.assetmanagement.repository.ExpenditureRepository;
import com.assetmanagement.security.CurrentUserService;
import com.assetmanagement.support.Filters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenditureService {
    private final ExpenditureRepository expenditureRepository;
    private final BaseRepository baseRepository;
    private final AssetService assetService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public ExpenditureService(
            ExpenditureRepository expenditureRepository,
            BaseRepository baseRepository,
            AssetService assetService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.expenditureRepository = expenditureRepository;
        this.baseRepository = baseRepository;
        this.assetService = assetService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    public List<Expenditure> list(Long baseId, String equipmentType, LocalDate startDate, LocalDate endDate) {
        Long scoped = currentUserService.scopedBaseId();
        Long effectiveBase = scoped != null ? scoped : baseId;
        List<Expenditure> rows = effectiveBase == null
                ? expenditureRepository.findAllByOrderByExpendedAtDesc()
                : expenditureRepository.findByBaseIdOrderByExpendedAtDesc(effectiveBase);
        rows = Filters.byBaseAndType(
                rows,
                effectiveBase,
                equipmentType,
                row -> row.getBase().getId(),
                row -> row.getAsset().getEquipmentType());
        return Filters.byInstantRange(rows, startDate, endDate, Expenditure::getExpendedAt);
    }

    @Transactional
    public Expenditure create(ExpenditureRequest request) {
        currentUserService.assertBaseAccess(request.baseId());
        Asset asset = assetService.get(request.assetId());
        if (!asset.getBase().getId().equals(request.baseId())) {
            throw new ApiException("Asset does not belong to the selected base");
        }
        Base base = baseRepository.findById(request.baseId()).orElseThrow(() -> new ApiException("Base not found"));
        assetService.adjustQuantity(asset, -request.quantity());
        Expenditure expenditure = new Expenditure();
        expenditure.setAsset(asset);
        expenditure.setBase(base);
        expenditure.setQuantity(request.quantity());
        expenditure.setReason(request.reason());
        expenditure.setExpendedAt(request.expendedAt() == null ? Instant.now() : request.expendedAt());
        Expenditure saved = expenditureRepository.save(expenditure);
        auditService.log(currentUserService.requireUser(), "CREATE_EXPENDITURE", "Expenditure", saved.getId(), "POST", "/api/expenditures");
        return saved;
    }
}
