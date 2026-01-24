package com.AgsCh.task_scheduler.repository;

import java.util.*;
import org.springframework.stereotype.Repository;

import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;

@Repository
public class TaskStore {

    private final Map<String, TaskRequestDTO> tasks = new LinkedHashMap<>();

    public void save(TaskRequestDTO task) {
        tasks.put(task.getName(), task);
    }

    public Map<String, TaskRequestDTO> findAll() {
        return Map.copyOf(tasks);
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
