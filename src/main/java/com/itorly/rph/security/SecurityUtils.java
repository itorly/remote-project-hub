package com.itorly.rph.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Helper to get current user (email) from SecurityContext
     * We’ll use this in services/controllers to know “who is calling”.
     */
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                || auth.getPrincipal() == null) {
            return null;
        }
        String name = auth.getName(); // we used email as username in CustomUserDetailsService
        if (name == null || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }
        return name;
    }
}
