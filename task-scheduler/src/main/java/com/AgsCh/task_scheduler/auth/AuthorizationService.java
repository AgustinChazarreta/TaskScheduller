package com.AgsCh.task_scheduler.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.AgsCh.task_scheduler.session.AdminSession;

@Component("authz")
public class AuthorizationService {

    private final AdminSession adminSession;

    public AuthorizationService(AdminSession adminSession) {
        this.adminSession = adminSession;
    }

    public boolean canAccessAdmin(Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true;
        }

        boolean isWebmaster = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_WEBMASTER"));

        return isWebmaster && adminSession.isImpersonating();
    }
}