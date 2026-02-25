package com.AgsCh.task_scheduler.service.admin;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.repository.ScheduleRunRepository;
import com.AgsCh.task_scheduler.service.solver.ScheduleService;

import jakarta.transaction.Transactional;

@Service
public class AdminScheduleService {

    private final ScheduleService solverService;
    private final ScheduleRunRepository scheduleRunRepository;

    public AdminScheduleService(
            ScheduleService solverService,
            ScheduleRunRepository scheduleRunRepository) {

        this.solverService = solverService;
        this.scheduleRunRepository = scheduleRunRepository;
    }

    /*
     * =========================
     * GENERAR Y RESOLVER
     * =========================
     */

    @Transactional
    public Schedule solve(Schedule schedule, House house) {

        if (schedule == null) {
            throw new BusinessException("No hay Schedule para resolver");
        }

        if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
            throw new BusinessException("El Schedule no tiene rango de fechas definido");
        }

        // 1️⃣ Archivar el último run (ACTIVE o INVALIDATED)
        scheduleRunRepository.findTopByHouse_IdOrderByCreatedAtDesc(house.getId())
                .ifPresent(run -> {
                    if (run.getStatus() != ScheduleRun.Status.ARCHIVED) {
                        run.setStatus(ScheduleRun.Status.ARCHIVED);
                        scheduleRunRepository.save(run);
                    }
                });

        // 2️⃣ Resolver
        Schedule solvedSchedule = solverService.solve(schedule);

        if (solvedSchedule.getScore() == null) {
            throw new BusinessException("El solver no devolvió score");
        }

        // 3️⃣ Crear nuevo ACTIVE
        ScheduleRun run = new ScheduleRun(
                solvedSchedule.getStartDate(),
                solvedSchedule.getEndDate(),
                solvedSchedule.getScore().toString(),
                house);

        run.setStatus(ScheduleRun.Status.ACTIVE);

        for (FunctionAssignment assignment : solvedSchedule.getFunctionAssignmentList()) {
            run.addAssignment(assignment);
        }

        scheduleRunRepository.save(run);

        return solvedSchedule;
    }

    /*
     * =========================
     * OBTENER RUN ACTIVO
     * =========================
     */

    public ScheduleRun getActiveRunByHouse(Long houseId) {
        Optional<ScheduleRun> run = scheduleRunRepository
                .findByHouseIdAndStatus(houseId, ScheduleRun.Status.ACTIVE);

        return run.orElse(null);
    }

    /*
     * =========================
     * INVALIDAR (archivar activo)
     * =========================
     */

    @Transactional
    public void invalidate(House house) {

        scheduleRunRepository
                .findByHouseIdAndStatus(house.getId(), ScheduleRun.Status.ACTIVE)
                .ifPresent(run -> {
                    run.setStatus(ScheduleRun.Status.INVALIDATED);
                    scheduleRunRepository.save(run);
                });
    }

    public ScheduleRun getLastRunByHouse(Long houseId) {
        return scheduleRunRepository
                .findTopByHouse_IdOrderByCreatedAtDesc(houseId)
                .orElse(null);
    }

    @Transactional
    public ScheduleRun createNewRun(Schedule schedule, House house) {
        if (schedule == null) {
            throw new BusinessException("No hay Schedule para crear nueva versión");
        }

        if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
            throw new BusinessException("El Schedule no tiene rango de fechas definido");
        }

        // 1️⃣ Archivar el último run activo o invalidado
        scheduleRunRepository.findTopByHouse_IdOrderByCreatedAtDesc(house.getId())
                .ifPresent(run -> {
                    if (run.getStatus() != ScheduleRun.Status.ARCHIVED) {
                        run.setStatus(ScheduleRun.Status.ARCHIVED);
                        scheduleRunRepository.save(run);
                    }
                });

        // 2️⃣ Crear nuevo ACTIVE run con las asignaciones ya editadas
        ScheduleRun newRun = new ScheduleRun(
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getScore() != null ? schedule.getScore().toString() : "0",
                house);
        newRun.setStatus(ScheduleRun.Status.ACTIVE);

        if (schedule.getFunctionAssignmentList() != null) {
            for (FunctionAssignment assignment : schedule.getFunctionAssignmentList()) {
                newRun.addAssignment(assignment);
            }
        }

        // 3️⃣ Guardar en DB
        scheduleRunRepository.save(newRun);

        return newRun;
    }
}
