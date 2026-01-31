package com.AgsCh.task_scheduler.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.service.admin.AdminService;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;

import jakarta.validation.Valid;
import java.util.List;

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
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("invalidated", scheduleService.isInvalidated());
        model.addAttribute("lastSolvedAt", scheduleService.getLastSolvedAt());
        model.addAttribute("hasSchedule", scheduleService.getCurrentSchedule() != null);

        // pasar listas de personas y tareas al dashboard
        List<Person> persons = adminService.listPersons();
        List<Task> tasks = adminService.listTasks();

        model.addAttribute("persons", persons);
        model.addAttribute("tasks", tasks);

        return "admin/dashboard";
    }

    // -------------------- SCHEDULE --------------------
    @PostMapping("/schedule/solve")
    public String solve(@Valid @ModelAttribute("scheduleRequest") ScheduleRequestDTO request) {
        // Genera y resuelve el schedule usando AdminService
        adminService.generateSchedule(request);
        return "redirect:/admin";
    }

    @PostMapping("/schedule/reset")
    public String reset() {
        adminService.resetSchedule();
        return "redirect:/admin";
    }
}
