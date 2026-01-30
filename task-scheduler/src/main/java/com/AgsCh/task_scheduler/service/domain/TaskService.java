package com.AgsCh.task_scheduler.service.domain;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    // -------- CREATE --------
    public Task create(TaskRequestDTO dto) {
        Task task = new Task(
            dto.getName(),
            dto.getAllowedCategories(),
            dto.getAssignedDays()
        );

        return repository.save(task);
    }

    // -------- READ --------
    public List<Task> findAll() {
        return repository.findAll();
    }

    public Task findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    // -------- UPDATE --------
    public void update(Long id, TaskRequestDTO dto) {
        Task task = findById(id);

        task.setName(dto.getName());
        task.setAllowedCategories(dto.getAllowedCategories());
        task.setAssignedDays(dto.getAssignedDays());

        repository.save(task);
    }

    // -------- DELETE --------
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
