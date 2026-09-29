package com.assetmanagement.service;

import com.assetmanagement.dto.UserRequest;
import com.assetmanagement.dto.UserResponse;
import com.assetmanagement.dto.UserUpdateRequest;
import com.assetmanagement.entity.Base;
import com.assetmanagement.entity.Role;
import com.assetmanagement.entity.User;
import com.assetmanagement.exception.ApiException;
import com.assetmanagement.repository.AuditLogRepository;
import com.assetmanagement.repository.BaseRepository;
import com.assetmanagement.repository.RoleRepository;
import com.assetmanagement.repository.UserRepository;
import com.assetmanagement.security.CurrentUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UserAdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BaseRepository baseRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;

    public UserAdminService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BaseRepository baseRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService,
            AuditService auditService,
            AuditLogRepository auditLogRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.baseRepository = baseRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.auditLogRepository = auditLogRepository;
    }

    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException("Email already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(resolveRole(request.role()));
        user.setBase(resolveBase(request.baseId()));
        UserResponse response = toResponse(userRepository.save(user));
        auditService.log(currentUserService.requireUser(), "CREATE_USER", "User", response.id(), "POST", "/api/users");
        return response;
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found"));
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ApiException("Email already exists");
        }
        user.setName(request.name());
        user.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setRole(resolveRole(request.role()));
        user.setBase(resolveBase(request.baseId()));
        UserResponse response = toResponse(userRepository.save(user));
        auditService.log(currentUserService.requireUser(), "UPDATE_USER", "User", id, "PUT", "/api/users/" + id);
        return response;
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found"));
        if (user.getEmail().equalsIgnoreCase("admin@example.com")) {
            throw new ApiException("The primary administrator account cannot be removed");
        }
        if (auditLogRepository.existsByUserId(id)) {
            throw new ApiException(
                    "This user has audit history and cannot be deleted - audit records must be retained for accountability");
        }
        userRepository.delete(user);
        auditService.log(currentUserService.requireUser(), "DELETE_USER", "User", id, "DELETE", "/api/users/" + id);
    }

    private Role resolveRole(String role) {
        return roleRepository.findByName(role.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException("Role not found"));
    }

    private Base resolveBase(Long baseId) {
        if (baseId == null) {
            return null;
        }
        return baseRepository.findById(baseId).orElseThrow(() -> new ApiException("Base not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                user.getBase() == null ? null : user.getBase().getId(),
                user.getBase() == null ? null : user.getBase().getName()
        );
    }
}
