package com.AgsCh.task_scheduler.controller.api;

import java.util.List;
import java.util.stream.Collectors;
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
    public Long create(@RequestBody TaskRequestDTO dto) {
        return service.create(dto).getId();
    }

    // -------- READ --------
    @GetMapping
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
    public void update(@PathVariable Long id, @RequestBody TaskRequestDTO dto) {
        service.update(id, dto);
    }

    // -------- DELETE --------
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
