package com.AgsCh.task_scheduler.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.AdminService;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWebController {

    private final AdminService adminService;
    private final AdminScheduleService scheduleService;

    public AdminWebController(AdminService adminService, AdminScheduleService scheduleService) {
        this.adminService = adminService;
        this.scheduleService = scheduleService;
    }

    /*
     * =========================
     * DASHBOARD
     * =========================
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        String username = authentication.getName();
        User user = adminService.findByUsername(username);

        Long houseId = user.getHouse().getId();
        var activeRun = scheduleService.getActiveRunByHouse(houseId);

        boolean isValid = activeRun != null;

        model.addAttribute("activeRun", activeRun);
        model.addAttribute("hasSchedule", isValid);
        model.addAttribute("isValid", isValid);
        model.addAttribute("lastSolvedAt",
                activeRun != null ? activeRun.getCreatedAt() : null);

        model.addAttribute("adminName", user.getUsername());
        model.addAttribute("houseName", user.getHouse().getName());
        model.addAttribute("role", user.getRole().name());

        return "admin/dashboard";
    }

    /*
     * =========================
     * GENERAR Y RESOLVER
     * =========================
     */
    @PostMapping("/schedule/solve")
    public String solve(@Valid @ModelAttribute("scheduleRequest") ScheduleRequestDTO request,
            @AuthenticationPrincipal User user,
            Model model) {

        try {
            adminService.generateAndSolve(request, user);
            return "redirect:/admin/dashboard?success";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/dashboard?error";
        }
    }

    /*
     * =========================
     * INVALIDAR SCHEDULE ACTIVO
     * =========================
     */
    @PostMapping("/schedule/invalidate")
    public String invalidate(@AuthenticationPrincipal User user) {

        scheduleService.invalidate(user.getHouse());

        return "redirect:/admin/dashboard?invalidated";
    }
}
