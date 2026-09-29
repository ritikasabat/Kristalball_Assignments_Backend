package com.assetmanagement.service;

import com.assetmanagement.dto.TransferRequest;
import com.assetmanagement.entity.Asset;
import com.assetmanagement.entity.Base;
import com.assetmanagement.entity.Transfer;
import com.assetmanagement.exception.ApiException;
import com.assetmanagement.repository.BaseRepository;
import com.assetmanagement.repository.TransferRepository;
import com.assetmanagement.security.CurrentUserService;
import com.assetmanagement.support.Filters;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransferService {
    private final TransferRepository transferRepository;
    private final BaseRepository baseRepository;
    private final AssetService assetService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public TransferService(
            TransferRepository transferRepository,
            BaseRepository baseRepository,
            AssetService assetService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.transferRepository = transferRepository;
        this.baseRepository = baseRepository;
        this.assetService = assetService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    public List<Transfer> list(Long baseId, String equipmentType, LocalDate startDate, LocalDate endDate) {
        Long scoped = currentUserService.scopedBaseId();
        List<Transfer> rows = scoped == null
                ? transferRepository.findAllByOrderByTransferDateDesc()
                : transferRepository.findByFromBaseIdOrToBaseIdOrderByTransferDateDesc(scoped, scoped);
        if (baseId != null && scoped == null) {
            List<Transfer> filtered = new ArrayList<>();
            for (Transfer row : rows) {
                if (baseId.equals(row.getFromBase().getId()) || baseId.equals(row.getToBase().getId())) {
                    filtered.add(row);
                }
            }
            rows = filtered;
        }
        rows = Filters.byBaseAndType(
                rows,
                null,
                equipmentType,
                row -> row.getFromBase().getId(),
                row -> row.getAsset().getEquipmentType());
        return Filters.byInstantRange(rows, startDate, endDate, Transfer::getTransferDate);
    }

    @Transactional
    public Transfer create(TransferRequest request) {
        if (request.fromBaseId().equals(request.toBaseId())) {
            throw new ApiException("From base and to base must be different");
        }
        Long scoped = currentUserService.scopedBaseId();
        if (scoped != null && !scoped.equals(request.fromBaseId()) && !scoped.equals(request.toBaseId())) {
            throw new AccessDeniedException("You can only transfer assets involving your assigned base");
        }
        Asset source = assetService.get(request.assetId());
        if (!source.getBase().getId().equals(request.fromBaseId())) {
            throw new ApiException("Selected asset does not belong to the source base");
        }
        Base from = baseRepository.findById(request.fromBaseId()).orElseThrow(() -> new ApiException("From base not found"));
        Base to = baseRepository.findById(request.toBaseId()).orElseThrow(() -> new ApiException("To base not found"));
        assetService.adjustQuantity(source, -request.quantity());
        Asset destination = assetService.findOrCreateInventory(to.getId(), source.getName(), source.getEquipmentType());
        assetService.adjustQuantity(destination, request.quantity());

        Transfer transfer = new Transfer();
        transfer.setAsset(source);
        transfer.setFromBase(from);
        transfer.setToBase(to);
        transfer.setQuantity(request.quantity());
        transfer.setTransferDate(request.transferDate() == null ? Instant.now() : request.transferDate());
        transfer.setRemarks(request.remarks());
        Transfer saved = transferRepository.save(transfer);
        auditService.log(currentUserService.requireUser(), "CREATE_TRANSFER", "Transfer", saved.getId(), "POST", "/api/transfers");
        return saved;
    }
}
