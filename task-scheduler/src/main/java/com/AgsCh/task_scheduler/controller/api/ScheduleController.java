package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionAssignmentResponseDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;
import com.AgsCh.task_scheduler.service.admin.AdminService;

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
                        PersonRepository personRepository,
                        AdminService adminService) {

                this.scheduleService = scheduleService;
                this.functionRepository = functionRepository;
                this.personRepository = personRepository;
                this.adminService = adminService;
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

                User user = getAuthenticatedUser(authentication);

                Schedule schedule = ScheduleMapper.toModel(
                                request,
                                functionRepository,
                                personRepository);

                Schedule solvedSchedule = scheduleService.solve(schedule, user.getHouse());

                return ScheduleMapper.toResponse(solvedSchedule);
        }

        /*
         * =========================
         * VER SCHEDULE ACTUAL
         * =========================
         */
        @GetMapping("/current")
        @PreAuthorize("hasAnyRole('ADMIN','USER')")
        public ScheduleResponseDTO current(@AuthenticationPrincipal User user) {

                validateUserAndHouse(user);

                ScheduleRun activeRun = scheduleService.getActiveRunByHouse(user.getHouse().getId());

                if (activeRun == null) {
                        throw new BusinessException("No hay schedule activo para esta House");
                }

                return ScheduleMapper.toResponse(activeRun);
        }

        /*
         * =========================
         * CREAR NUEVA VERSIÓN (drag & drop)
         * =========================
         */
        @PostMapping("/create-new-run")
        @PreAuthorize("hasRole('ADMIN')")
        public ScheduleResponseDTO createNewRun(
                        @Valid @RequestBody List<FunctionAssignmentResponseDTO> dtos,
                        Authentication authentication) {

                User user = getAuthenticatedUser(authentication);

                List<Function> functions = functionRepository.findAll();
                List<Person> persons = personRepository.findAll();

                Schedule schedule = ScheduleMapper.toModelFromAssignments(dtos, functions, persons);

                ScheduleRun newRun = scheduleService.createNewRun(schedule, user.getHouse());

                return ScheduleMapper.toResponse(newRun);
        }

        /*
         * =========================
         * MÉTODOS PRIVADOS AUXILIARES
         * =========================
         */

        private User getAuthenticatedUser(Authentication authentication) {

                if (authentication == null) {
                        throw new BusinessException("Usuario no autenticado");
                }

                String username = authentication.getName();
                User user = adminService.findByUsername(username);

                if (user == null) {
                        throw new BusinessException("Usuario autenticado no encontrado");
                }

                validateUserAndHouse(user);

                return user;
        }

        private void validateUserAndHouse(User user) {

                if (user == null) {
                        throw new BusinessException("Usuario no autenticado");
                }

                if (user.getHouse() == null) {
                        throw new BusinessException("El usuario no tiene House asignada");
                }
        }
}