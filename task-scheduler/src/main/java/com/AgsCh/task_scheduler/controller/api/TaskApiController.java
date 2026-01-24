package com.AgsCh.task_scheduler.controller.api;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Category;
import com.AgsCh.task_scheduler.repository.TaskStore;
import com.AgsCh.task_scheduler.util.normalizer.TaskRefactor;
import com.AgsCh.task_scheduler.util.word.WordParser;

import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {

    private final TaskStore store;

    public TaskApiController(TaskStore store) {
        this.store = store;
    }

    /*
     * =====================================================
     * PARSE WORD → CREA TAREAS EN MEMORIA
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
     * LISTAR TAREAS (PERSISTEN AL RECARGAR)
     * =====================================================
     */

    @GetMapping
    public Map<String, TaskRequestDTO> list() {
        return store.findAll();
    }

    /*
     * =====================================================
     * ACTUALIZAR CATEGORÍAS
     * =====================================================
     */

    @PutMapping("/{name}/categories")
    public void updateCategories(
            @PathVariable String name,
            @RequestBody @NotEmpty Set<Category> categories) {

        TaskRequestDTO task = store.findByName(name);

        if (task == null) {
            throw new BusinessException("Tarea inexistente: " + name);
        }

        task.setAllowedCategories(categories);
        store.update(name, task);
    }

    /*
     * =====================================================
     * ELIMINAR / LIMPIAR
     * =====================================================
     */

    @DeleteMapping("/{name}")
    public void delete(@PathVariable String name) {
        store.delete(name);
    }

    @PostMapping
    public void saveTasks(@RequestBody @NotEmpty List<TaskRequestDTO> tasks) {
        tasks.forEach(store::save);
    }

}
