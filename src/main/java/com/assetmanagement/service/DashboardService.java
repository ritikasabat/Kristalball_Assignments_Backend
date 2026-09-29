package com.assetmanagement.service;

import com.assetmanagement.dto.DashboardResponse;
import com.assetmanagement.dto.NetMovementResponse;
import com.assetmanagement.entity.Assignment;
import com.assetmanagement.entity.Expenditure;
import com.assetmanagement.entity.Purchase;
import com.assetmanagement.entity.Transfer;
import com.assetmanagement.repository.AssignmentRepository;
import com.assetmanagement.repository.ExpenditureRepository;
import com.assetmanagement.repository.PurchaseRepository;
import com.assetmanagement.repository.TransferRepository;
import com.assetmanagement.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(
            PurchaseRepository purchaseRepository,
            TransferRepository transferRepository,
            AssignmentRepository assignmentRepository,
            ExpenditureRepository expenditureRepository,
            CurrentUserService currentUserService
    ) {
        this.purchaseRepository = purchaseRepository;
        this.transferRepository = transferRepository;
        this.assignmentRepository = assignmentRepository;
        this.expenditureRepository = expenditureRepository;
        this.currentUserService = currentUserService;
    }

    public DashboardResponse summary(LocalDate startDate, LocalDate endDate, Long baseId, String equipmentType) {
        Filter filter = resolve(startDate, endDate, baseId, equipmentType);
        int purchases = purchases(filter, false).stream().mapToInt(Purchase::getQuantity).sum();
        int transferIn = transfersIn(filter, false).stream().mapToInt(Transfer::getQuantity).sum();
        int transferOut = transfersOut(filter, false).stream().mapToInt(Transfer::getQuantity).sum();
        int assigned = assignments(filter, false).stream().mapToInt(Assignment::getQuantity).sum();
        int expended = expenditures(filter, false).stream().mapToInt(Expenditure::getQuantity).sum();
        int net = purchases + transferIn - transferOut;
        int openingPurchases = purchases(filter, true).stream().mapToInt(Purchase::getQuantity).sum();
        int openingIn = transfersIn(filter, true).stream().mapToInt(Transfer::getQuantity).sum();
        int openingOut = transfersOut(filter, true).stream().mapToInt(Transfer::getQuantity).sum();
        int openingAssigned = assignments(filter, true).stream().mapToInt(Assignment::getQuantity).sum();
        int openingExpended = expenditures(filter, true).stream().mapToInt(Expenditure::getQuantity).sum();
        int opening = openingPurchases + openingIn - openingOut - openingAssigned - openingExpended;
        int closing = opening + net - assigned - expended;
        return new DashboardResponse(opening, closing, net, purchases, transferIn, transferOut, assigned, expended);
    }

    public NetMovementResponse netMovement(LocalDate startDate, LocalDate endDate, Long baseId, String equipmentType) {
        Filter filter = resolve(startDate, endDate, baseId, equipmentType);
        List<Purchase> purchases = purchases(filter, false);
        List<Transfer> in = transfersIn(filter, false);
        List<Transfer> out = transfersOut(filter, false);
        int purchaseQty = purchases.stream().mapToInt(Purchase::getQuantity).sum();
        int inQty = in.stream().mapToInt(Transfer::getQuantity).sum();
        int outQty = out.stream().mapToInt(Transfer::getQuantity).sum();
        return new NetMovementResponse(
                purchaseQty,
                inQty,
                outQty,
                purchaseQty + inQty - outQty,
                purchases.stream().map(p -> new NetMovementResponse.PurchaseLine(
                        p.getPurchaseDate().format(DATE),
                        p.getAsset().getName(),
                        p.getAsset().getEquipmentType(),
                        p.getQuantity()
                )).toList(),
                in.stream().map(t -> new NetMovementResponse.TransferInLine(
                        DATE.format(t.getTransferDate().atZone(ZoneOffset.UTC)),
                        t.getFromBase().getName(),
                        t.getAsset().getName(),
                        t.getQuantity()
                )).toList(),
                out.stream().map(t -> new NetMovementResponse.TransferOutLine(
                        DATE.format(t.getTransferDate().atZone(ZoneOffset.UTC)),
                        t.getToBase().getName(),
                        t.getAsset().getName(),
                        t.getQuantity()
                )).toList()
        );
    }

    private Filter resolve(LocalDate startDate, LocalDate endDate, Long baseId, String equipmentType) {
        Long scoped = currentUserService.scopedBaseId();
        Long effectiveBase = scoped != null ? scoped : baseId;
        String type = (equipmentType == null || equipmentType.isBlank() || "ALL".equalsIgnoreCase(equipmentType))
                ? null
                : equipmentType.toUpperCase(Locale.ROOT);
        return new Filter(startDate, endDate, effectiveBase, type);
    }

    private List<Purchase> purchases(Filter filter, boolean beforeStart) {
        return purchaseRepository.findAll().stream()
                .filter(p -> matchesBase(filter.baseId, p.getBase().getId()))
                .filter(p -> matchesType(filter.type, p.getAsset().getEquipmentType()))
                .filter(p -> matchesDate(filter, p.getPurchaseDate(), beforeStart))
                .toList();
    }

    private List<Transfer> transfersIn(Filter filter, boolean beforeStart) {
        return transferRepository.findAll().stream()
                .filter(t -> matchesBase(filter.baseId, t.getToBase().getId()))
                .filter(t -> matchesType(filter.type, t.getAsset().getEquipmentType()))
                .filter(t -> matchesInstant(filter, t.getTransferDate(), beforeStart))
                .toList();
    }

    private List<Transfer> transfersOut(Filter filter, boolean beforeStart) {
        return transferRepository.findAll().stream()
                .filter(t -> matchesBase(filter.baseId, t.getFromBase().getId()))
                .filter(t -> matchesType(filter.type, t.getAsset().getEquipmentType()))
                .filter(t -> matchesInstant(filter, t.getTransferDate(), beforeStart))
                .toList();
    }

    private List<Assignment> assignments(Filter filter, boolean beforeStart) {
        return assignmentRepository.findAll().stream()
                .filter(a -> matchesBase(filter.baseId, a.getBase().getId()))
                .filter(a -> matchesType(filter.type, a.getAsset().getEquipmentType()))
                .filter(a -> matchesInstant(filter, a.getAssignedAt(), beforeStart))
                .toList();
    }

    private List<Expenditure> expenditures(Filter filter, boolean beforeStart) {
        return expenditureRepository.findAll().stream()
                .filter(e -> matchesBase(filter.baseId, e.getBase().getId()))
                .filter(e -> matchesType(filter.type, e.getAsset().getEquipmentType()))
                .filter(e -> matchesInstant(filter, e.getExpendedAt(), beforeStart))
                .toList();
    }

    private boolean matchesBase(Long filterBase, Long actual) {
        return filterBase == null || filterBase.equals(actual);
    }

    private boolean matchesType(String type, String actual) {
        return type == null || type.equalsIgnoreCase(actual);
    }

    private boolean matchesDate(Filter filter, LocalDate date, boolean beforeStart) {
        if (beforeStart) {
            return filter.startDate != null && date.isBefore(filter.startDate);
        }
        if (filter.startDate != null && date.isBefore(filter.startDate)) {
            return false;
        }
        return filter.endDate == null || !date.isAfter(filter.endDate);
    }

    private boolean matchesInstant(Filter filter, Instant instant, boolean beforeStart) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        return matchesDate(filter, date, beforeStart);
    }

    private record Filter(LocalDate startDate, LocalDate endDate, Long baseId, String type) {
    }
}
