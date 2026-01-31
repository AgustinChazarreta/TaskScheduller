package com.AgsCh.task_scheduler.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public Long create(@RequestBody TaskRequestDTO dto) {
        return service.create(dto).getId();
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
            t.getAssignedDays()
        ))
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
}
