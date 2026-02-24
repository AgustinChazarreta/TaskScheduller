package com.AgsCh.task_scheduler.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.AdminService;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final AdminService adminService;
    private final AdminScheduleService scheduleService;

    public AdminApiController(AdminService adminService,
            AdminScheduleService scheduleService) {
        this.adminService = adminService;
        this.scheduleService = scheduleService;
    }

    /*
     * =========================
     * GENERAR Y RESOLVER
     * =========================
     */
    @PostMapping("/schedule/solve")
    public ScheduleResponseDTO solveSchedule(
            @RequestBody ScheduleRequestDTO request,
            @AuthenticationPrincipal User user) {

        var solved = adminService.generateAndSolve(request, user);

        return ScheduleMapper.toResponse(solved);
    }

    /*
     * =========================
     * VER ACTIVO
     * =========================
     */
    @GetMapping("/schedule/current")
    public ScheduleResponseDTO current(@AuthenticationPrincipal User user) {

        var run = scheduleService.getActiveRunByHouse(user.getHouse().getId());

        if (run == null) {
            return null;
        }

        return ScheduleMapper.toResponse(run);
    }

    /*
     * =========================
     * INVALIDAR
     * =========================
     */
    @PostMapping("/schedule/invalidate")
    public void invalidate(@AuthenticationPrincipal User user) {
        scheduleService.invalidate(user.getHouse());
    }

    @GetMapping("/schedule/status")
    public ResponseEntity<?> getScheduleStatus(Authentication authentication) {

        String username = authentication.getName();
        User user = adminService.findByUsername(username);

        var lastRun = scheduleService
                .getLastRunByHouse(user.getHouse().getId());

        Map<String, Object> response = new HashMap<>();

        if (lastRun == null) {
            response.put("invalidated", true);
            response.put("lastSolvedAt", null);
        } else {
            response.put("invalidated",
                    lastRun.getStatus() != ScheduleRun.Status.ACTIVE);
            response.put("lastSolvedAt",
                    lastRun.getCreatedAt());
        }

        return ResponseEntity.ok(response);
    }

}
