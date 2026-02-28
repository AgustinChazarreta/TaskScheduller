package com.AgsCh.task_scheduler.service.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.repository.ScheduleRunRepository;

@Service
public class WebmasterScheduleService {

    private final ScheduleRunRepository scheduleRunRepository;

    public WebmasterScheduleService(ScheduleRunRepository scheduleRunRepository) {
        this.scheduleRunRepository = scheduleRunRepository;
    }

    /*
     * =========================
     * LISTADO GLOBAL
     * =========================
     */

    public List<ScheduleRun> getAllRuns() {
        return scheduleRunRepository.findAllByOrderByCreatedAtDesc();
    }

    /*
     * =========================
     * ÚLTIMO RUN GLOBAL
     * =========================
     */

    public ScheduleRun getLastGlobalRun() {
        Optional<ScheduleRun> run = scheduleRunRepository.findTopByOrderByCreatedAtDesc();

        return run.orElse(null);
    }

    /*
     * =========================
     * RUNS DEL MES ACTUAL (KPI)
     * =========================
     */

    public long countCurrentMonthRuns() {

        LocalDate now = LocalDate.now();

        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();

        LocalDateTime startOfNextMonth = now.plusMonths(1)
                .withDayOfMonth(1)
                .atStartOfDay();

        return scheduleRunRepository
                .countByCreatedAtBetween(startOfMonth, startOfNextMonth);
    }

    /*
     * =========================
     * RUNS ACTIVOS GLOBALES
     * =========================
     */

    public long countActiveRuns() {
        return scheduleRunRepository
                .countByStatus(ScheduleRun.Status.ACTIVE);
    }
}