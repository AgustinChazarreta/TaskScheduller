package com.AgsCh.task_scheduler.service.admin;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.service.solver.ScheduleService;

/**
 * AdminScheduleService mantiene el Schedule en memoria
 * y delega la resolución a OptaPlanner vía ScheduleService.
 * Solo se gestiona metadata y estado del Schedule en memoria.
 */
@Service
public class AdminScheduleService {

    private final ScheduleService solverService;

    private Schedule currentSchedule;
    private boolean invalidated = true;
    private LocalDateTime lastSolvedAt;

    public AdminScheduleService(ScheduleService solverService) {
        this.solverService = solverService;
    }

    /* =========================
     * STATE
     * ========================= */

    public boolean isInvalidated() {
        return invalidated;
    }

    public LocalDateTime getLastSolvedAt() {
        return lastSolvedAt;
    }

    public Schedule getCurrentSchedule() {
        return currentSchedule;
    }

    /* =========================
     * LIFECYCLE
     * ========================= */

    /**
     * Carga un Schedule base (personas y tareas actuales)
     */
    public void loadSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new BusinessException("No se puede cargar un Schedule nulo");
        }
        this.currentSchedule = schedule;
        this.invalidated = true;
        this.lastSolvedAt = null;
    }

    /**
     * Marca el Schedule como desactualizado
     * (después de cambios en Person o Task)
     */
    public void invalidate() {
        this.invalidated = true;
    }

    /**
     * Resuelve el Schedule actual usando OptaPlanner
     * @return Schedule resuelto
     */
    public Schedule solve() {
        if (currentSchedule == null) {
            throw new BusinessException("No hay un Schedule cargado para resolver");
        }

        currentSchedule = solverService.solve(currentSchedule);
        invalidated = false;
        lastSolvedAt = LocalDateTime.now();
        return currentSchedule;
    }

    /**
     * Borra el Schedule actual de memoria
     */
    public void reset() {
        currentSchedule = null;
        invalidated = true;
        lastSolvedAt = null;
    }
}
