package com.AgsCh.task_scheduler.controller;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.*;
import com.AgsCh.task_scheduler.dto.request.requestAdapter.PlanningPeriodRequestDTO;
import com.AgsCh.task_scheduler.dto.request.requestAdapter.ScheduleRequestDTOAdapter;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.model.Category;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.service.ScheduleService;
import com.AgsCh.task_scheduler.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    // ===============================
    // ENDPOINT CLÁSICO (JSON PURO)
    // ===============================
    @PostMapping("/solve")
    public ScheduleResponseDTO solve(@RequestBody ScheduleRequestDTO request) {

        Schedule schedule = ScheduleMapper.toModel(request);
        Schedule solved = scheduleService.solve(schedule);

        return ScheduleMapper.toResponse(solved);
    }

    // ===============================
    // ENDPOINT WORD + JSON
    // ===============================
    @PostMapping(value = "/from-word", consumes = "multipart/form-data")
    public ResponseEntity<ScheduleResponseDTO> scheduleFromWord(
            @RequestPart("file") MultipartFile word,
            @RequestPart("period") String periodJson,
            @RequestPart("persons") String personsJson) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();

            // 1️⃣ JSON → DTOs
            PlanningPeriodRequestDTO periodDTO = mapper.readValue(periodJson, PlanningPeriodRequestDTO.class);

            List<PersonRequestDTO> persons = mapper.readValue(personsJson, new TypeReference<>() {
            });

            // 2️⃣ WORD → TASKS
            Map<String, List<String>> parsedTasks = WordParser.parseTasks(word);

            Map<String, List<DayOfWeek>> normalizedDays = TaskRefactor.refactorDays(parsedTasks);

            Map<String, Set<Category>> categories = UserInputSimulator.enrich(normalizedDays);

            List<Task> tasks = TaskAssembler.buildTasks(normalizedDays, categories);

            // 3️⃣ ARMADO DEL REQUEST FINAL
            ScheduleRequestDTO scheduleRequest = ScheduleRequestDTOAdapter.adapt(periodDTO, persons, tasks);

            // 4️⃣ SOLVER
            Schedule schedule = ScheduleMapper.toModel(scheduleRequest);
            Schedule solved = scheduleService.solve(schedule);

            ScheduleResponseDTO response = ScheduleMapper.toResponse(solved);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Error processing scheduling request", e);
        }
    }
}
