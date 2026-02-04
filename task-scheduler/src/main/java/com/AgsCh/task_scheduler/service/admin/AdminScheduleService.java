package com.AgsCh.task_scheduler.service.admin;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.repository.FunctionAssignmentRepository;
import com.AgsCh.task_scheduler.repository.ScheduleRunRepository;
import com.AgsCh.task_scheduler.service.solver.ScheduleService;

import jakarta.transaction.Transactional;

@Service
public class AdminScheduleService {

    private final ScheduleService solverService;
    private final ScheduleRunRepository scheduleRunRepository;
    private final FunctionAssignmentRepository assignmentRepository;

    private Schedule currentSchedule;
    private boolean invalidated = true;
    private LocalDateTime lastSolvedAt;

    public AdminScheduleService(
            ScheduleService solverService,
            ScheduleRunRepository scheduleRunRepository,
            FunctionAssignmentRepository assignmentRepository) {

        this.solverService = solverService;
        this.scheduleRunRepository = scheduleRunRepository;
        this.assignmentRepository = assignmentRepository;
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

        // 1️⃣ resolver con OptaPlanner
        currentSchedule = solverService.solve(currentSchedule);

        // 2️⃣ archivar el run activo anterior
        scheduleRunRepository.archiveActiveRun();

        // 3️⃣ crear nuevo ScheduleRun
        ScheduleRun run = new ScheduleRun(
                currentSchedule.getStartDate(),
                currentSchedule.getEndDate(),
                currentSchedule.getScore().toString());
        run.activate();
        scheduleRunRepository.save(run);

        // 4️⃣ asociar assignments al run
        for (FunctionAssignment assignment : currentSchedule.getFunctionAssignmentList()) {
            assignment.setScheduleRun(run);
        }

        // 5️⃣ persistir resultados
        assignmentRepository.saveAll(currentSchedule.getFunctionAssignmentList());

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

    public LocalDateTime getLastSolvedAt() {
        return lastSolvedAt;
    }
}
