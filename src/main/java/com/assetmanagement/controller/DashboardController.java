package com.assetmanagement.controller;

import com.assetmanagement.dto.DashboardResponse;
import com.assetmanagement.dto.NetMovementResponse;
import com.assetmanagement.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) String equipmentType
    ) {
        return dashboardService.summary(startDate, endDate, baseId, equipmentType);
    }

    @GetMapping("/net-movement")
    public NetMovementResponse netMovement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) String equipmentType
    ) {
        return dashboardService.netMovement(startDate, endDate, baseId, equipmentType);
    }
}
