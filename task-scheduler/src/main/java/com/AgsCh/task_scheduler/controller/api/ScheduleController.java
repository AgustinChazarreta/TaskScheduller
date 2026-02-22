package com.AgsCh.task_scheduler.controller.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;
import com.AgsCh.task_scheduler.service.admin.AdminService;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

        private final AdminScheduleService scheduleService;
        private final FunctionRepository functionRepository;
        private final PersonRepository personRepository;
        private final AdminService adminService;

        public ScheduleController(
                        AdminScheduleService scheduleService,
                        FunctionRepository functionRepository,
                        AdminService adminService,
                        PersonRepository personRepository) {

                this.scheduleService = scheduleService;
                this.functionRepository = functionRepository;
                this.adminService = adminService;
                this.personRepository = personRepository;
        }

        /*
         * =========================
         * RESOLVER SCHEDULE
         * =========================
         */
        @PostMapping("/solve")
        @PreAuthorize("hasRole('ADMIN')")
        public ScheduleResponseDTO solve(
                        @Valid @RequestBody ScheduleRequestDTO request,
                        Authentication authentication) {

                try {
                        // 🔐 Obtener usuario autenticado correctamente
                        String username = authentication.getName();
                        User user = adminService.findByUsername(username);

                        if (user == null) {
                                throw new BusinessException("Usuario autenticado no encontrado");
                        }

                        // 1️⃣ DTO → dominio
                        Schedule schedule = ScheduleMapper.toModel(
                                        request,
                                        functionRepository,
                                        personRepository);

                        // 2️⃣ Resolver y persistir por house
                        Schedule solvedSchedule = scheduleService.solve(schedule, user.getHouse());

                        // 3️⃣ Respuesta
                        return ScheduleMapper.toResponse(solvedSchedule);

                } catch (Exception e) {
                        throw new BusinessException("Error al resolver el schedule", e);
                }
        }

        /*
         * =========================
         * VER SCHEDULE ACTUAL
         * =========================
         */
        @GetMapping("/current")
        @PreAuthorize("hasAnyRole('ADMIN','USER')")
        public ScheduleResponseDTO current(@AuthenticationPrincipal User user) {

                var activeRun = scheduleService.getActiveRunByHouse(user.getHouse().getId());

                if (activeRun == null) {
                        throw new BusinessException("No hay schedule activo para esta House");
                }

                return ScheduleMapper.toResponse(activeRun);
        }
}
