package com.assetmanagement.service;

import com.assetmanagement.dto.AssetRequest;
import com.assetmanagement.entity.Asset;
import com.assetmanagement.entity.Base;
import com.assetmanagement.exception.ApiException;
import com.assetmanagement.repository.AssetRepository;
import com.assetmanagement.repository.BaseRepository;
import com.assetmanagement.security.CurrentUserService;
import com.assetmanagement.support.Filters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AssetService {
    private final AssetRepository assetRepository;
    private final BaseRepository baseRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public AssetService(
            AssetRepository assetRepository,
            BaseRepository baseRepository,
            CurrentUserService currentUserService,
            AuditService auditService) {
        this.assetRepository = assetRepository;
        this.baseRepository = baseRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    public List<Asset> list(Long baseId, String equipmentType) {
        Long scoped = currentUserService.scopedBaseId();
        Long effectiveBase = scoped != null ? scoped : baseId;
        List<Asset> assets = effectiveBase == null
                ? assetRepository.findAll()
                : assetRepository.findByBaseId(effectiveBase);
        return Filters.byBaseAndType(
                assets,
                effectiveBase,
                equipmentType,
                asset -> asset.getBase().getId(),
                Asset::getEquipmentType);
    }

    public Asset get(Long id) {
        Asset asset = assetRepository.findById(id).orElseThrow(() -> new ApiException("Asset not found"));
        currentUserService.assertBaseAccess(asset.getBase().getId());
        return asset;
    }

    @Transactional
    public Asset create(AssetRequest request) {
        currentUserService.assertBaseAccess(request.baseId());
        Base base = baseRepository.findById(request.baseId()).orElseThrow(() -> new ApiException("Base not found"));
        Asset asset = new Asset();
        asset.setAssetCode(resolveCode(request.assetCode()));
        asset.setName(request.name());
        asset.setEquipmentType(request.equipmentType().toUpperCase(Locale.ROOT));
        asset.setQuantity(request.quantity());
        asset.setBase(base);
        asset.setStatus(request.status() == null ? "AVAILABLE" : request.status());
        Asset saved = assetRepository.save(asset);
        auditService.log(currentUserService.requireUser(), "CREATE_ASSET", "Asset", saved.getId(), "POST",
                "/api/assets");
        return saved;
    }

    @Transactional
    public Asset update(Long id, AssetRequest request) {
        Asset asset = get(id);
        currentUserService.assertBaseAccess(request.baseId());
        Base base = baseRepository.findById(request.baseId()).orElseThrow(() -> new ApiException("Base not found"));
        if (request.assetCode() != null && !request.assetCode().isBlank()) {
            asset.setAssetCode(request.assetCode());
        }
        asset.setName(request.name());
        asset.setEquipmentType(request.equipmentType().toUpperCase(Locale.ROOT));
        asset.setQuantity(request.quantity());
        asset.setBase(base);
        if (request.status() != null) {
            asset.setStatus(request.status());
        }
        Asset saved = assetRepository.save(asset);
        auditService.log(currentUserService.requireUser(), "UPDATE_ASSET", "Asset", saved.getId(), "PUT",
                "/api/assets/" + id);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Asset asset = get(id);
        assetRepository.delete(asset);
        auditService.log(currentUserService.requireUser(), "DELETE_ASSET", "Asset", id, "DELETE", "/api/assets/" + id);
    }

    public Asset findOrCreateInventory(Long baseId, String name, String equipmentType) {
        return assetRepository.findByNameIgnoreCaseAndEquipmentTypeIgnoreCaseAndBaseId(name, equipmentType, baseId)
                .orElseGet(() -> {
                    Base base = baseRepository.findById(baseId).orElseThrow(() -> new ApiException("Base not found"));
                    Asset asset = new Asset();
                    asset.setAssetCode(resolveCode(null));
                    asset.setName(name);
                    asset.setEquipmentType(equipmentType.toUpperCase(Locale.ROOT));
                    asset.setQuantity(0);
                    asset.setBase(base);
                    asset.setStatus("AVAILABLE");
                    return assetRepository.save(asset);
                });
    }

    public void adjustQuantity(Asset asset, int delta) {
        int next = asset.getQuantity() + delta;
        if (next < 0) {
            throw new ApiException("Insufficient quantity for asset " + asset.getName());
        }
        asset.setQuantity(next);
        asset.setStatus(next == 0 ? "DEPLETED" : "AVAILABLE");
        assetRepository.save(asset);
    }

    private String resolveCode(String requested) {
        if (requested != null && !requested.isBlank() && !assetRepository.existsByAssetCode(requested)) {
            return requested;
        }
        String code;
        do {
            code = "AST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (assetRepository.existsByAssetCode(code));
        return code;
    }
}
