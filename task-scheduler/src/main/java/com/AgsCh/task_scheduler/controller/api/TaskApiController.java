package com.AgsCh.task_scheduler.controller.api;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.dto.response.TaskResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Category;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.repository.TaskRepository;
import com.AgsCh.task_scheduler.util.normalizer.TaskRefactor;
import com.AgsCh.task_scheduler.util.word.WordParser;

import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {

    private final TaskRepository repository;

    public TaskApiController(TaskRepository repository) {
        this.repository = repository;
    }

    /*
     * =====================================================
     * PARSE WORD → SOLO DEVUELVE PREVIEW (NO PERSISTE)
     * =====================================================
     */

    @PostMapping(value = "/from-word", consumes = "multipart/form-data")
    public Map<String, Set<DayOfWeek>> parseFromWord(
            @RequestPart("file") MultipartFile file) {

        Map<String, List<String>> raw = WordParser.parseTasks(file);
        return TaskRefactor.refactorDays(raw);
    }

    /*
     * =====================================================
     * LISTAR TAREAS (DESDE DB)
     * =====================================================
     */

    @GetMapping
    public List<TaskResponseDTO> list() {
        return repository.findAll().stream()
                .map(t -> new TaskResponseDTO(
                        t.getId(),
                        t.getName(),
                        t.getAllowedCategories(),
                        t.getAssignedDays()))
                .toList();
    }

    /*
     * =====================================================
     * CREAR TAREAS (BATCH O INDIVIDUAL)
     * =====================================================
     */

    @PostMapping
    public void saveTasks(@RequestBody @NotEmpty List<TaskRequestDTO> tasks) {
        tasks.forEach(dto -> {
            Task task = new Task(
                    dto.getName(),
                    dto.getAllowedCategories(),
                    dto.getAssignedDays());
            repository.save(task);
        });
    }

    /*
     * =====================================================
     * ACTUALIZAR CATEGORÍAS
     * =====================================================
     */

    @PutMapping("/{id}/categories")
    public void updateCategories(
            @PathVariable Long id,
            @RequestBody @NotEmpty Set<Category> categories) {

        Task task = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Tarea inexistente: " + id));

        task.setAllowedCategories(categories);
        repository.save(task);
    }

    /*
     * =====================================================
     * ACTUALIZAR TAREA COMPLETA
     * =====================================================
     */
    @PutMapping("/{id}")
    public void updateTask(@PathVariable Long id, @RequestBody TaskRequestDTO dto) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Tarea inexistente: " + id));

        task.setName(dto.getName());
        task.setAllowedCategories(dto.getAllowedCategories());
        task.setAssignedDays(dto.getAssignedDays());

        repository.save(task);
    }

    /*
     * =====================================================
     * ELIMINAR
     * =====================================================
     */

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
