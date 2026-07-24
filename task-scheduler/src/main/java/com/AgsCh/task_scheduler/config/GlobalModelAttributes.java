package com.AgsCh.task_scheduler.config;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.AgsCh.task_scheduler.auth.AuthorizationService;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.AdminService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final AdminService adminService;
    private final AuthorizationService authz;

    public GlobalModelAttributes(
            AdminService adminService,
            AuthorizationService authz) {

        this.adminService = adminService;
        this.authz = authz;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {

        if (authentication == null) {
            return;
        }

        String username = authentication.getName();
        User user = adminService.findByUsername(username);

        model.addAttribute("isAdminView", authz.canAccessAdmin(authentication));

        if (user != null && user.getHouse() != null) {
            model.addAttribute("houseName", user.getHouse().getName());
            model.addAttribute("role", user.getRole().name());
            model.addAttribute("userName", user.getUsername());
        }
    }
}