package com.AgsCh.task_scheduler.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;

import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.AdminService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final AdminService adminService;

    public GlobalModelAttributes(AdminService adminService) {
        this.adminService = adminService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {

        if (authentication == null) return;

        String username = authentication.getName();
        User user = adminService.findByUsername(username);

        if (user != null && user.getHouse() != null) {
            model.addAttribute("houseName", user.getHouse().getName());
            model.addAttribute("role", user.getRole().name());
            model.addAttribute("userName", user.getUsername());
        }
    }
}