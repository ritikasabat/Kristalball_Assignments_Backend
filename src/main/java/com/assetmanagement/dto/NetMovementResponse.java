package com.assetmanagement.dto;

import java.util.List;

public record NetMovementResponse(
        int purchasesTotal,
        int transferInTotal,
        int transferOutTotal,
        int netMovement,
        List<PurchaseLine> purchases,
        List<TransferInLine> transferIn,
        List<TransferOutLine> transferOut
) {
    public record PurchaseLine(String date, String asset, String equipmentType, int quantity) {
    }

    public record TransferInLine(String date, String fromBase, String asset, int quantity) {
    }

    public record TransferOutLine(String date, String toBase, String asset, int quantity) {
    }
}
