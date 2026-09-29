package com.assetmanagement.service;

import com.assetmanagement.dto.AuthResponse;
import com.assetmanagement.dto.LoginRequest;
import com.assetmanagement.entity.User;
import com.assetmanagement.repository.UserRepository;
import com.assetmanagement.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(user);
        auditService.log(user, "LOGIN", "User", user.getId(), "POST", "/api/auth/login");
        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                user.getBase() == null ? null : user.getBase().getId(),
                user.getBase() == null ? null : user.getBase().getName()
        );
    }
}
