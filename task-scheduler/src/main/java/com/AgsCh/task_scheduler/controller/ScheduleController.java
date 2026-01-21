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
import com.AgsCh.task_scheduler.dto.request.requestAdapter.*;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Category;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.service.ScheduleService;
import com.AgsCh.task_scheduler.util.word.WordParser;
import com.AgsCh.task_scheduler.util.assembler.*;
import com.AgsCh.task_scheduler.util.normalizer.TaskRefactor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

        private final ScheduleService scheduleService;
        private final ObjectMapper objectMapper;

        public ScheduleController(ScheduleService scheduleService, ObjectMapper objectMapper) {
                this.scheduleService = scheduleService;
                this.objectMapper = objectMapper;
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
                        @RequestPart("persons") String personsJson,
                        @RequestPart("taskCategories") String taskCategoriesJson) {

                try {

                        // Logs para verificar recepción
                        System.out.println("=== Datos recibidos en /api/schedule/from-word ===");
                        System.out.println("Archivo Word: " + (word != null ? word.getOriginalFilename() : "null"));
                        System.out.println("Período JSON: " + periodJson);
                        System.out.println("Personas JSON: " + personsJson);

                        // =========================
                        // 1️⃣ JSON → DTOs (Data Transfer Objects)
                        // =========================
                        PlanningPeriodRequestDTO periodDTO = objectMapper.readValue(periodJson,
                                        PlanningPeriodRequestDTO.class);

                        List<PersonRequestDTO> persons = objectMapper.readValue(personsJson, new TypeReference<>() {
                        });

                        // =========================
                        // 2️⃣ WORD → TASKS
                        // =========================
                        Map<String, List<String>> parsedTasks = WordParser.parseTasks(word);

                        Map<String, Set<DayOfWeek>> normalizedDays = TaskRefactor.refactorDays(parsedTasks);

                        Map<String, Set<Category>> categories = objectMapper.readValue(taskCategoriesJson,
                                        new TypeReference<Map<String, Set<Category>>>() {
                                        });

                        List<Task> tasks = TaskAssembler.buildTasks(normalizedDays, categories);

                        // =========================
                        // 3️⃣ ARMADO DEL REQUEST FINAL
                        // =========================
                        ScheduleRequestDTO scheduleRequest = ScheduleRequestDTOAdapter.adapt(periodDTO, persons, tasks);

                        // =========================
                        // 4️⃣ SOLVERS
                        // =========================
                        Schedule schedule = ScheduleMapper.toModel(scheduleRequest);

                        System.out.println("=== Schedule inicial ===");

                        System.out.println("Personas:");
                        schedule.getPersonList().forEach(
                                        p -> System.out.println(" - " + p.getName() + " (" + p.getCategory() + ")"));

                        System.out.println("Tareas:");
                        schedule.getTaskList().forEach(t -> System.out
                                        .println(" - " + t.getName() + " días permitidos: " + t.getAssignedDays()));

                        System.out.println("Asignaciones:");
                        schedule.getTaskAssignmentList()
                                        .forEach(a -> System.out.println(" - Tarea: " + a.getTask().getName()
                                                        + ", Fecha: " + a.getDate() + ", Persona: " + a.getPerson()));

                        Schedule solved = scheduleService.solve(schedule);

                        ScheduleResponseDTO response = ScheduleMapper.toResponse(solved);

                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        throw new BusinessException("Error processing scheduling request", e);
                }
        }
        

        
        @PostMapping(value = "/tasks/from-word", consumes = "multipart/form-data")
        public Map<String, Set<DayOfWeek>> parseTasks(
                        @RequestPart("file") MultipartFile word) {

                System.out.println("=== PARSE TASKS ===");
                System.out.println("Archivo: " + word.getOriginalFilename());

                Map<String, List<String>> raw = WordParser.parseTasks(word);
                System.out.println("RAW: " + raw);

                Map<String, Set<DayOfWeek>> result = TaskRefactor.refactorDays(raw);
                System.out.println("NORMALIZED: " + result);

                return result;
        }

}
