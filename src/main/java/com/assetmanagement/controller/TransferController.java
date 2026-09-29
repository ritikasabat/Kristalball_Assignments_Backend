package com.assetmanagement.controller;

import com.assetmanagement.dto.TransferRequest;
import com.assetmanagement.entity.Transfer;
import com.assetmanagement.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public List<Transfer> list(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) String equipmentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return transferService.list(baseId, equipmentType, startDate, endDate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer create(@Valid @RequestBody TransferRequest request) {
        return transferService.create(request);
    }
}
