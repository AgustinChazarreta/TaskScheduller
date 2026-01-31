package com.AgsCh.task_scheduler.controller.api;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.dto.response.TaskResponseDTO;
import com.AgsCh.task_scheduler.service.domain.TaskService;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {

    private final TaskService service;

    public TaskApiController(TaskService service) {
        this.service = service;
    }

    // -------- CREATE --------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void createTasks(@RequestBody List<TaskRequestDTO> tasks) {
        service.createTasks(tasks);
    }

    // -------- READ --------
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<TaskResponseDTO> list() {
        return service.findAll().stream()
                .map(t -> new TaskResponseDTO(
                        t.getId(),
                        t.getName(),
                        t.getAllowedCategories(),
                        t.getAssignedDays()))
                .collect(Collectors.toList());
    }

    // -------- UPDATE --------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable Long id, @RequestBody TaskRequestDTO dto) {
        service.update(id, dto);
    }

    // -------- DELETE --------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // -------- LOAD WORD --------
    @PostMapping(value = "/from-word", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Set<DayOfWeek>> parseFromWord(
            @RequestParam("file") MultipartFile file) {
        return service.parseTasksFromWord(file);
    }

}
