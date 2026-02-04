package com.AgsCh.task_scheduler.service.admin;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.service.solver.ScheduleService;

@Service
public class AdminService {

    private final AdminScheduleService adminScheduleService;
    private final ScheduleService solverService;
    private final FunctionRepository taskRepository;
    private final PersonRepository personRepository;

    public AdminService(
            AdminScheduleService adminScheduleService,
            ScheduleService solverService,
            FunctionRepository taskRepository,
            PersonRepository personRepository) {

        this.adminScheduleService = adminScheduleService;
        this.solverService = solverService;
        this.taskRepository = taskRepository;
        this.personRepository = personRepository;
    }

    /* =========================
     * SCHEDULE
     * ========================= */

    /**
     * Genera y resuelve un nuevo Schedule
     */
    public Schedule generateAndSolve(ScheduleRequestDTO request) {

        // 1️⃣ DTO → dominio
        Schedule schedule = ScheduleMapper.toModel(
                request,
                taskRepository,
                personRepository
        );

        // 2️⃣ cargar en memoria
        adminScheduleService.loadSchedule(schedule);

        // 3️⃣ resolver
        Schedule solved = solverService.solve(schedule);

        return solved;
    }

    /**
     * Obtiene el schedule actual (si existe)
     */
    public Schedule getCurrentSchedule() {
        return adminScheduleService.getCurrentSchedule();
    }

    /**
     * Resuelve nuevamente el schedule cargado
     */
    public Schedule resolveCurrent() {
        return adminScheduleService.solve();
    }

    /**
     * Borra el schedule actual
     */
    public void resetSchedule() {
        adminScheduleService.reset();
    }

    /**
     * Estado de validez
     */
    public boolean isScheduleInvalidated() {
        return adminScheduleService.isInvalidated();
    }
}
