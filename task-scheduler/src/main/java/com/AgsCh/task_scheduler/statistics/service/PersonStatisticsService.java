package com.AgsCh.task_scheduler.statistics.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.repository.FunctionAssignmentRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.statistics.dto.FunctionStatsDTO;
import com.AgsCh.task_scheduler.statistics.dto.PersonStatisticsDTO;

@Service
public class PersonStatisticsService {

        private final FunctionAssignmentRepository assignmentRepo;
        private final PersonRepository personRepo;

        public PersonStatisticsService(
                        FunctionAssignmentRepository assignmentRepo,
                        PersonRepository personRepo) {

                this.assignmentRepo = assignmentRepo;
                this.personRepo = personRepo;
        }

        public PersonStatisticsDTO getStatistics(Long personId) {

                Person person = personRepo.findById(personId)
                                .orElseThrow(() -> new RuntimeException("Person not found"));

                long total = assignmentRepo.countAllByPersonId(personId);

                LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
                long lastMonth = assignmentRepo.countByPersonIdFromDate(personId, oneMonthAgo);

                List<FunctionStatsDTO> breakdown = assignmentRepo.countByFunctionGrouped(personId)
                                .stream()
                                .map(row -> new FunctionStatsDTO(
                                                (String) row[0],
                                                (Long) row[1]))
                                .collect(Collectors.toList());

                LocalDate lastDate = assignmentRepo.findLastAssignmentDate(personId);

                return new PersonStatisticsDTO(
                                person.getId(),
                                person.getFullName(),
                                total,
                                lastMonth,
                                breakdown,
                                lastDate);
        }
}