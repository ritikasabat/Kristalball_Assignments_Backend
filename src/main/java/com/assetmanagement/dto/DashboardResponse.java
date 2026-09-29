package com.assetmanagement.dto;

public record DashboardResponse(
        int openingBalance,
        int closingBalance,
        int netMovement,
        int purchases,
        int transferIn,
        int transferOut,
        int assignedAssets,
        int expendedAssets
) {
}
