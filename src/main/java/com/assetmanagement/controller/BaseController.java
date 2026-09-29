package com.assetmanagement.controller;

import com.assetmanagement.dto.BaseRequest;
import com.assetmanagement.entity.Base;
import com.assetmanagement.repository.BaseRepository;
import com.assetmanagement.security.CurrentUserService;
import com.assetmanagement.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bases")
public class BaseController {
    private final BaseRepository baseRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public BaseController(BaseRepository baseRepository, CurrentUserService currentUserService, AuditService auditService) {
        this.baseRepository = baseRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<Base> list() {
        Long scoped = currentUserService.scopedBaseId();
        if (scoped == null) {
            return baseRepository.findAll();
        }
        return baseRepository.findById(scoped).stream().toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Base create(@Valid @RequestBody BaseRequest request) {
        Base base = new Base();
        base.setName(request.name());
        base.setLocation(request.location());
        Base saved = baseRepository.save(base);
        auditService.log(currentUserService.requireUser(), "CREATE_BASE", "Base", saved.getId(), "POST", "/api/bases");
        return saved;
    }
}
