package com.AgsCh.task_scheduler.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.User;

public class CustomUserDetails implements UserDetails {

    private final User user;

    // House sobre la que está trabajando actualmente
    private House activeHouse;

    // Si el Webmaster está administrando una House
    private boolean impersonating = false;

    // Rol efectivo (normalmente coincide con el del User)
    private Role effectiveRole;

    public CustomUserDetails(User user) {
        this.user = user;
        this.activeHouse = user.getHouse();
        this.effectiveRole = user.getRole();
    }

    public User getUser() {
        return user;
    }

    public House getActiveHouse() {
        return activeHouse;
    }

    public void setActiveHouse(House activeHouse) {
        this.activeHouse = activeHouse;
    }

    public boolean isImpersonating() {
        return impersonating;
    }

    public void setImpersonating(boolean impersonating) {
        this.impersonating = impersonating;
    }

    public Role getEffectiveRole() {
        return effectiveRole;
    }

    public void setEffectiveRole(Role effectiveRole) {
        this.effectiveRole = effectiveRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + effectiveRole.name());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}