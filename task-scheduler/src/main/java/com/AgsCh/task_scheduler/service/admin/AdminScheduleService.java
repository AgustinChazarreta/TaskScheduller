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

        // 1️⃣ Archivar run activo de esa house (si existe)
        scheduleRunRepository.archiveActiveRunByHouse(
                ScheduleRun.Status.ARCHIVED,
                ScheduleRun.Status.ACTIVE,
                house.getId());

        // 2️⃣ Resolver con OptaPlanner
        Schedule solvedSchedule = solverService.solve(schedule);

        if (solvedSchedule.getScore() == null) {
            throw new BusinessException("El solver no devolvió score");
        }

        // 3️⃣ Crear nuevo run
        ScheduleRun run = new ScheduleRun(
                solvedSchedule.getStartDate(),
                solvedSchedule.getEndDate(),
                solvedSchedule.getScore().toString(),
                house);

        run.setHouse(house); // 🔥 AISLAMIENTO MULTI-HOUSE
        run.setStatus(ScheduleRun.Status.ACTIVE);
        run.setScore(solvedSchedule.getScore().toString());

        // 4️⃣ Asociar assignments
        for (FunctionAssignment assignment : solvedSchedule.getFunctionAssignmentList()) {
            run.addAssignment(assignment);
        }

        // 5️⃣ Guardar
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
        scheduleRunRepository.archiveActiveRunByHouse(
                ScheduleRun.Status.ARCHIVED,
                ScheduleRun.Status.ACTIVE,
                house.getId());
    }
}
