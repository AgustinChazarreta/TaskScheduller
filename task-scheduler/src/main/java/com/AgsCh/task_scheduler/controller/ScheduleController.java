package com.AgsCh.task_scheduler.controller;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.*;
import com.AgsCh.task_scheduler.dto.request.requestAdapter.*;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.service.ScheduleService;
import com.AgsCh.task_scheduler.util.word.WordParser;
import com.AgsCh.task_scheduler.util.normalizer.TaskRefactor;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

        private final ScheduleService scheduleService;

        public ScheduleController(ScheduleService scheduleService) {
                this.scheduleService = scheduleService;
        }

        // ===============================
        // ENDPOINT ADAPTADO PARA FRONTEND
        // ===============================
        // ===============================
        @PostMapping("/solve")
        public ScheduleResponseDTO solve(@RequestBody SolveRequestDTO request) {
                try {
                        // Logs para verificar recepción
                        System.out.println("=== Datos recibidos en /api/schedule/solve ===");
                        System.out.println("Período: " + request.getStartDate() + " a " + request.getEndDate());
                        System.out.println("Personas seleccionadas: " + request.getSelectedPersons().size());
                        System.out.println("Tareas: " + request.getTasks().size());

                        // =========================
                        // 1️⃣ JSON → DTOs (Data Transfer Objects)
                        // =========================
                        PlanningPeriodRequestDTO periodDTO = new PlanningPeriodRequestDTO();
                        periodDTO.setStartDate(request.getStartDate());
                        periodDTO.setEndDate(request.getEndDate());

                        // Personas ya vienen como List<Person>, convertir a PersonRequestDTO
                        List<PersonRequestDTO> persons = request.getSelectedPersons().stream()
                                        .map(p -> {
                                                PersonRequestDTO dto = new PersonRequestDTO();
                                                dto.setName(p.getName());
                                                dto.setCategory(p.getCategory());
                                                dto.setBirthDate(p.getBirthDate());
                                                dto.setAvailableDays(p.getAvailableDays());
                                                return dto;
                                        })
                                        .collect(Collectors.toList());

                        // Tareas ya vienen como List<Task>
                        ///////////////////////////////////// 
                        ///////////////////////////////////// 
                        ///////////////////////////////////// 
//////////////////////////ToDo: converitr igual a DTO///////////////////////////////////
                        ///////////////////////////////////// 
                        ///////////////////////////////////// 
                        ///////////////////////////////////// 
                        List<Task> tasks = request.getTasks();

                        // =========================
                        // 2️⃣ ARMADO DEL REQUEST FINAL
                        // =========================
                        ScheduleRequestDTO scheduleRequest = ScheduleRequestDTOAdapter.adapt(periodDTO, persons, tasks);

                        // =========================
                        // 3️⃣ SOLVERS
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

                        return response;

                } catch (Exception e) {
                        e.printStackTrace(); // Para ver el stack completo
                        throw new BusinessException("Error al resolver el schedule con datos del frontend", e);
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