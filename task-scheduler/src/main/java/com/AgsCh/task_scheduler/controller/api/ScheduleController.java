package com.AgsCh.task_scheduler.controller.api;

import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.service.ScheduleService;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

        private final ScheduleService scheduleService;

        public ScheduleController(
                ScheduleService scheduleService) { this.scheduleService = scheduleService; }

        /**
         * Resuelve el schedule a partir de los datos enviados por el frontend.
         */
        @PostMapping("/solve")
        public ScheduleResponseDTO solve(@RequestBody ScheduleRequestDTO request) {
                try {
                        // 1️⃣ Caso de uso: construir el Schedule de dominio
                        Schedule schedule = ScheduleMapper.toModel(request);

                        // 2️⃣ Resolver el problema con OptaPlanner
                        Schedule solvedSchedule = scheduleService.solve(schedule);

                        // 3️⃣ Mapear a DTO de respuesta
                        return ScheduleMapper.toResponse(solvedSchedule);

                } catch (Exception e) {
                        throw new BusinessException(
                                        "Error al resolver el schedule con los datos proporcionados",
                                        e);
                }
        }
}
