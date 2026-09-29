package com.assetmanagement.service;

import com.assetmanagement.dto.PurchaseRequest;
import com.assetmanagement.entity.Asset;
import com.assetmanagement.entity.Base;
import com.assetmanagement.entity.Purchase;
import com.assetmanagement.exception.ApiException;
import com.assetmanagement.repository.BaseRepository;
import com.assetmanagement.repository.PurchaseRepository;
import com.assetmanagement.security.CurrentUserService;
import com.assetmanagement.support.Filters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final BaseRepository baseRepository;
    private final AssetService assetService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            BaseRepository baseRepository,
            AssetService assetService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.purchaseRepository = purchaseRepository;
        this.baseRepository = baseRepository;
        this.assetService = assetService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    public List<Purchase> list(Long baseId, String equipmentType, LocalDate startDate, LocalDate endDate) {
        Long scoped = currentUserService.scopedBaseId();
        Long effectiveBase = scoped != null ? scoped : baseId;
        List<Purchase> rows = effectiveBase == null
                ? purchaseRepository.findAllByOrderByPurchaseDateDesc()
                : purchaseRepository.findByBaseIdOrderByPurchaseDateDesc(effectiveBase);
        rows = Filters.byBaseAndType(
                rows,
                effectiveBase,
                equipmentType,
                row -> row.getBase().getId(),
                row -> row.getAsset().getEquipmentType());
        return Filters.byDateRange(rows, startDate, endDate, Purchase::getPurchaseDate);
    }

    @Transactional
    public Purchase create(PurchaseRequest request) {
        currentUserService.assertBaseAccess(request.baseId());
        Base base = baseRepository.findById(request.baseId()).orElseThrow(() -> new ApiException("Base not found"));
        Asset asset = assetService.findOrCreateInventory(
                request.baseId(), request.assetName(), request.equipmentType().toUpperCase(Locale.ROOT));
        assetService.adjustQuantity(asset, request.quantity());
        Purchase purchase = new Purchase();
        purchase.setAsset(asset);
        purchase.setBase(base);
        purchase.setQuantity(request.quantity());
        purchase.setPurchaseDate(request.purchaseDate());
        purchase.setCost(request.cost());
        purchase.setSupplier(request.supplier());
        purchase.setDescription(request.description());
        Purchase saved = purchaseRepository.save(purchase);
        auditService.log(currentUserService.requireUser(), "CREATE_PURCHASE", "Purchase", saved.getId(), "POST", "/api/purchases");
        return saved;
    }
}
