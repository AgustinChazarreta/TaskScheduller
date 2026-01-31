package com.AgsCh.task_scheduler.controller.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.TaskRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

        private final AdminScheduleService scheduleService;
        private final TaskRepository taskRepository;
        private final PersonRepository personRepository;

        public ScheduleController(AdminScheduleService scheduleService, TaskRepository taskRepository, PersonRepository personRepository) {
                this.scheduleService = scheduleService;
                this.taskRepository = taskRepository;
                this.personRepository = personRepository;
        }

        /**
         * Resuelve el schedule a partir de los datos enviados por el frontend.
         */
        @PostMapping("/solve")
        @PreAuthorize("hasAnyRole('ADMIN','USER')")
        public ScheduleResponseDTO solve(@Valid @RequestBody ScheduleRequestDTO request) {
                try {
                        // 1️⃣ Convertir DTO a Schedule de dominio con entidades persistidas
                        Schedule schedule = ScheduleMapper.toModel(request, taskRepository, personRepository);

                        // 2️⃣ Resolver con OptaPlanner
                        scheduleService.loadSchedule(schedule);
                        Schedule solvedSchedule = scheduleService.solve();

                        // 3️⃣ Mapear a DTO de respuesta
                        return ScheduleMapper.toResponse(solvedSchedule);

                } catch (Exception e) {
                        throw new BusinessException(
                                        "Error al resolver el schedule con los datos proporcionados",
                                        e);
                }
        }

        // -------- VER SCHEDULE ACTUAL --------
        @GetMapping("/current")
        @PreAuthorize("hasAnyRole('ADMIN','USER')")
        public ScheduleResponseDTO current() {
                Schedule current = scheduleService.getCurrentSchedule();
                return ScheduleMapper.toResponse(current);
        }

}
