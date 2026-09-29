package com.assetmanagement.controller;

import com.assetmanagement.dto.ExpenditureRequest;
import com.assetmanagement.entity.Expenditure;
import com.assetmanagement.service.ExpenditureService;
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
@RequestMapping("/api/expenditures")
public class ExpenditureController {
    private final ExpenditureService expenditureService;

    public ExpenditureController(ExpenditureService expenditureService) {
        this.expenditureService = expenditureService;
    }

    @GetMapping
    public List<Expenditure> list(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) String equipmentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return expenditureService.list(baseId, equipmentType, startDate, endDate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expenditure create(@Valid @RequestBody ExpenditureRequest request) {
        return expenditureService.create(request);
    }
}
