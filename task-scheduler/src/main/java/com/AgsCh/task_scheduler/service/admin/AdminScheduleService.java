package com.AgsCh.task_scheduler.service.admin;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.repository.ScheduleRunRepository;
import com.AgsCh.task_scheduler.service.solver.ScheduleService;

import jakarta.transaction.Transactional;

@Service
public class AdminScheduleService {

    private final ScheduleService solverService;
    private final ScheduleRunRepository scheduleRunRepository;

    private Schedule currentSchedule;
    private boolean invalidated = true;
    private LocalDateTime lastSolvedAt;

    public AdminScheduleService(
            ScheduleService solverService,
            ScheduleRunRepository scheduleRunRepository) {

        this.solverService = solverService;
        this.scheduleRunRepository = scheduleRunRepository;
    }

    /*
     * =========================
     * LIFECYCLE
     * =========================
     */

    public void loadSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new BusinessException("No se puede cargar un Schedule nulo");
        }
        this.currentSchedule = schedule;
        this.invalidated = true;
        this.lastSolvedAt = null;
    }

    @Transactional
    public Schedule solve() {

        if (currentSchedule == null) {
            throw new BusinessException("No hay un Schedule cargado para resolver");
        }

        if (currentSchedule.getStartDate() == null || currentSchedule.getEndDate() == null) {
            throw new BusinessException("El Schedule no tiene rango de fechas definido");
        }

        // 1️⃣ Archivar run activo anterior (si existe)
        scheduleRunRepository.archiveActiveRun(
                ScheduleRun.Status.ARCHIVED,
                ScheduleRun.Status.ACTIVE);

        // 2️⃣ Resolver con OptaPlanner
        currentSchedule = solverService.solve(currentSchedule);

        if (currentSchedule.getScore() == null) {
            throw new BusinessException("El solver no devolvió score");
        }

        // 3️⃣ Crear nuevo run (queda ACTIVE por defecto)
        ScheduleRun run = new ScheduleRun(
                currentSchedule.getStartDate(),
                currentSchedule.getEndDate());

        run.setStatus(ScheduleRun.Status.ACTIVE);
        run.setScore(currentSchedule.getScore().toString());

        // 4️⃣ Asociar assignments
        for (FunctionAssignment assignment : currentSchedule.getFunctionAssignmentList()) {
            run.addAssignment(assignment);
        }

        // 5️⃣ Guardar
        scheduleRunRepository.save(run);

        invalidated = false;
        lastSolvedAt = LocalDateTime.now();

        return currentSchedule;
    }

    public void reset() {
        currentSchedule = null;
        invalidated = true;
        lastSolvedAt = null;
    }

    public Schedule getCurrentSchedule() {
        return currentSchedule;
    }

    public boolean isInvalidated() {
        return invalidated;
    }

    @Transactional
    public void invalidate() {

        scheduleRunRepository.archiveActiveRun(
                ScheduleRun.Status.ARCHIVED,
                ScheduleRun.Status.ACTIVE);

        this.invalidated = true;
    }

    public LocalDateTime getLastSolvedAt() {
        return lastSolvedAt;
    }
}
