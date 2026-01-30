package com.AgsCh.task_scheduler.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.service.admin.AdminService;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final AdminService adminService;
    private final AdminScheduleService scheduleService;

    public AdminWebController(AdminService adminService,
            AdminScheduleService scheduleService) {
        this.adminService = adminService;
        this.scheduleService = scheduleService;
    }

    // -------------------- DASHBOARD --------------------
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("invalidated", scheduleService.isInvalidated());
        model.addAttribute("lastSolvedAt", scheduleService.getLastSolvedAt());
        model.addAttribute("hasSchedule", scheduleService.getCurrentSchedule() != null);

        // opcional: pasar lista de personas y tareas
        model.addAttribute("persons", adminService.listPersons());
        model.addAttribute("tasks", adminService.listTasks());

        return "admin/dashboard";
    }

    // -------------------- SCHEDULE --------------------
    @PostMapping("/schedule/solve")
    public String solve(@Valid @RequestBody ScheduleRequestDTO request) {
        adminService.generateSchedule(request);
        return "redirect:/admin";
    }

    @PostMapping("/schedule/reset")
    public String reset() {
        adminService.resetSchedule();
        return "redirect:/admin";
    }
}
