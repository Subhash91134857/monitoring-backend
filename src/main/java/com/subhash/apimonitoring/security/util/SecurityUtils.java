package com.subhash.apimonitoring.security.util;


import com.subhash.apimonitoring.security.model.CustomUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public boolean isCurrentUser(Long userId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserPrincipal) {
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) principal;
            return userPrincipal.getId().equals(userId);
        }

        return false;
    }

    public Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) principal).getId();
        }

        return null;
    }

    public String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) principal).getEmail();
        }

        return null;
    }
}
