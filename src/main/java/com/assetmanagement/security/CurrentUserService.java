package com.assetmanagement.security;

import com.assetmanagement.entity.User;
import com.assetmanagement.exception.ApiException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    public User requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ApiException("Authentication required");
        }
        return user;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(requireUser().getRole().getName());
    }

    public boolean isCommander() {
        return "BASE_COMMANDER".equals(requireUser().getRole().getName());
    }

    public boolean isLogistics() {
        return "LOGISTICS_OFFICER".equals(requireUser().getRole().getName());
    }

    public Long scopedBaseId() {
        User user = requireUser();
        if ("ADMIN".equals(user.getRole().getName())) {
            return null;
        }
        return user.getBase() == null ? null : user.getBase().getId();
    }

    public void assertBaseAccess(Long baseId) {
        if (isAdmin()) {
            return;
        }
        Long scoped = scopedBaseId();
        if (scoped == null) {
            if (isCommander()) {
                throw new AccessDeniedException("Base commander is not assigned to a base");
            }
            return;
        }
        if (baseId != null && !scoped.equals(baseId)) {
            throw new AccessDeniedException("You can only access data for your assigned base");
        }
    }
}
