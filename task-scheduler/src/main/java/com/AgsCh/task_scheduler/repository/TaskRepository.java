package com.AgsCh.task_scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.AgsCh.task_scheduler.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
