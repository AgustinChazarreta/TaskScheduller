package com.AgsCh.task_scheduler.service.domain;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.repository.TaskRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;
import com.AgsCh.task_scheduler.util.normalizer.TaskRefactor;
import com.AgsCh.task_scheduler.util.word.WordParser;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final AdminScheduleService scheduleService;

    public TaskService(TaskRepository repository, AdminScheduleService scheduleService) {
        this.repository = repository;
        this.scheduleService = scheduleService;
    }

    // -------- CREATE --------
    public Task create(TaskRequestDTO dto) {
        Task task = new Task(
                dto.getName(),
                dto.getAllowedCategories(),
                dto.getAssignedDays());
        scheduleService.invalidate();
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
        
        scheduleService.invalidate();
        repository.save(task);
    }
    
    // -------- DELETE --------
    public void delete(Long id) {
        repository.deleteById(id);
        scheduleService.invalidate();
    }

    // -------- LOAD WORD --------
    public Map<String, Set<DayOfWeek>> parseTasksFromWord(MultipartFile file) {

        if (file.isEmpty()) {
            throw new BusinessException("Archivo vacío");
        }

        Map<String, List<String>> raw = WordParser.parseTasks(file);
        return TaskRefactor.refactorDays(raw);
    }

    // -------- CREATE TASKS--------
    public void createTasks(List<TaskRequestDTO> tasks) {
        tasks.forEach(this::create);
    }
}
