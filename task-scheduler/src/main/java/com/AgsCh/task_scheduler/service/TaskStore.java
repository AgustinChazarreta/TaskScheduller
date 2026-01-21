package com.AgsCh.task_scheduler.service;

import java.util.*;
import org.springframework.stereotype.Component;
import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;

@Component
public class TaskStore {

    private final Map<String, TaskRequestDTO> tasks = new LinkedHashMap<>();

    public void save(TaskRequestDTO task) {
        tasks.put(task.getName(), task);
    }

    public Map<String, TaskRequestDTO> findAll() {
        return tasks;
    }

    public TaskRequestDTO findByName(String name) {
        return tasks.get(name);
    }

    public void update(String name, TaskRequestDTO task) {
        tasks.put(name, task);
    }

    public void delete(String name) {
        tasks.remove(name);
    }
}
