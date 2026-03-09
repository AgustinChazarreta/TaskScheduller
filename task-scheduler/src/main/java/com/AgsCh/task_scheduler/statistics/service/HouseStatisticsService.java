package com.AgsCh.task_scheduler.statistics.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.repository.FunctionAssignmentRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.ScheduleRunRepository;
import com.AgsCh.task_scheduler.statistics.dto.FunctionStatsDTO;
import com.AgsCh.task_scheduler.statistics.dto.HouseStatisticsDTO;
import com.AgsCh.task_scheduler.statistics.dto.MonthlyStatsDTO;
import com.AgsCh.task_scheduler.statistics.dto.PersonStatisticsDTO;

@Service
public class HouseStatisticsService {

        private final FunctionAssignmentRepository assignmentRepo;
        private final ScheduleRunRepository runRepo;
        private final PersonRepository personRepo;

        public HouseStatisticsService(
                        FunctionAssignmentRepository assignmentRepo,
                        ScheduleRunRepository runRepo,
                        PersonRepository personRepo) {

                this.assignmentRepo = assignmentRepo;
                this.runRepo = runRepo;
                this.personRepo = personRepo;
        }

        public HouseStatisticsDTO buildHouseStatistics(Long houseId) {

                // 🔹 Buscar último run
                ScheduleRun lastRun = runRepo
                                .findTopByHouse_IdOrderByCreatedAtDesc(houseId)
                                .orElse(null);

                if (lastRun == null) {
                        return new HouseStatisticsDTO(
                                        houseId,
                                        0,
                                        0,
                                        List.of(),
                                        List.of(),
                                        List.of());
                }

                // 🔹 Assignments del último run
                List<FunctionAssignment> assignmentsLastRun = assignmentRepo.findByScheduleRun_Id(lastRun.getId());

                // 🔹 Total assignments SOLO del último run
                long totalAssignments = assignmentsLastRun.size();

                // 🔹 Runs históricos
                long historicalRuns = runRepo.countByHouse_Id(houseId);

                // 🔹 Personas
                List<Person> people = personRepo.findByHouseId(houseId);

                // ================= PEOPLE STATS =================

                Map<Long, List<FunctionAssignment>> assignmentsGroupedByPerson = assignmentsLastRun.stream()
                                .filter(FunctionAssignment::isAssigned)
                                .collect(Collectors.groupingBy(
                                                fa -> fa.getPerson().getId()));

                List<PersonStatisticsDTO> peopleStats = people.stream()
                                .map(person -> {

                                        List<FunctionAssignment> personAssignments = assignmentsGroupedByPerson
                                                        .getOrDefault(
                                                                        person.getId(),
                                                                        List.of());

                                        long total = personAssignments.size();

                                        // 🔹 Breakdown por función
                                        List<FunctionStatsDTO> breakdown = personAssignments.stream()
                                                        .collect(Collectors.groupingBy(
                                                                        fa -> fa.getFunction().getName(),
                                                                        Collectors.counting()))
                                                        .entrySet()
                                                        .stream()
                                                        .map(e -> new FunctionStatsDTO(
                                                                        e.getKey(),
                                                                        e.getValue()))
                                                        .toList();

                                        // 🔹 Última fecha dentro del run
                                        java.time.LocalDate lastDate = personAssignments.stream()
                                                        .map(FunctionAssignment::getDate)
                                                        .max(java.util.Comparator.naturalOrder())
                                                        .orElse(null);

                                        return new PersonStatisticsDTO(
                                                        person.getId(),
                                                        person.getFullName(),
                                                        total,
                                                        0, // totalLastMonth (se puede agregar después)
                                                        breakdown,
                                                        lastDate);
                                })
                                .toList();

                // ================= FUNCTION STATS =================

                List<FunctionStatsDTO> functionStats = assignmentsLastRun.stream()
                                .filter(FunctionAssignment::isAssigned)
                                .collect(Collectors.groupingBy(
                                                fa -> fa.getFunction().getName(),
                                                Collectors.counting()))
                                .entrySet()
                                .stream()
                                .map(e -> new FunctionStatsDTO(
                                                e.getKey(),
                                                e.getValue()))
                                .toList();

                // ================= MONTHLY STATS =================

                List<MonthlyStatsDTO> monthlyStats = assignmentRepo
                                .countAssignmentsByRun(lastRun.getId())
                                .stream()
                                .map(row -> {

                                        int year = ((Number) row[0]).intValue();
                                        int month = ((Number) row[1]).intValue();
                                        long count = ((Number) row[2]).longValue();

                                        String formattedMonth = String.format("%04d-%02d", year, month);

                                        return new MonthlyStatsDTO(
                                                        formattedMonth,
                                                        count);
                                })
                                .toList();

                return new HouseStatisticsDTO(
                                houseId,
                                totalAssignments,
                                historicalRuns,
                                peopleStats,
                                functionStats,
                                monthlyStats);
        }
}