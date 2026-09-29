package com.assetmanagement.controller;

import com.assetmanagement.dto.AssetRequest;
import com.assetmanagement.entity.Asset;
import com.assetmanagement.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<Asset> list(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) String equipmentType
    ) {
        return assetService.list(baseId, equipmentType);
    }

    @GetMapping("/{id}")
    public Asset get(@PathVariable Long id) {
        return assetService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Asset create(@Valid @RequestBody AssetRequest request) {
        return assetService.create(request);
    }

    @PutMapping("/{id}")
    public Asset update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        return assetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assetService.delete(id);
    }
}
