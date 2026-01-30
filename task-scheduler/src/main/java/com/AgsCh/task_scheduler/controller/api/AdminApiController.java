package com.AgsCh.task_scheduler.controller.api;

import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.service.admin.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AdminService adminService;

    public AdminApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    // -------------------- SCHEDULE --------------------
    @PostMapping("/schedule/solve")
    public Schedule solveSchedule(@RequestBody ScheduleRequestDTO request) {
        return adminService.generateSchedule(request);
    }

    @PostMapping("/schedule/reset")
    public void resetSchedule() {
        adminService.resetSchedule();
    }

    @GetMapping("/schedule/status")
    public ScheduleStatus getStatus() {
        return new ScheduleStatus(adminService.isScheduleInvalidated());
    }

    public static class ScheduleStatus {
        private final boolean invalidated;

        public ScheduleStatus(boolean invalidated) {
            this.invalidated = invalidated;
        }

        public boolean isInvalidated() {
            return invalidated;
        }
    }
}
