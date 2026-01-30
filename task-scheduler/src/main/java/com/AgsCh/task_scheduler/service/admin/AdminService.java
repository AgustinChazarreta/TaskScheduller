package com.AgsCh.task_scheduler.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.TaskRepository;
import com.AgsCh.task_scheduler.service.domain.PersonService;
import com.AgsCh.task_scheduler.service.domain.TaskService;
import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.service.solver.ScheduleService;

@Service
public class AdminService {

    private final PersonService personService;
    private final TaskService taskService;
    private final AdminScheduleService scheduleService;
    private final ScheduleService solverService;
    private final TaskRepository taskRepository;
    private final PersonRepository personRepository;

    public AdminService(PersonService personService,
            TaskService taskService,
            AdminScheduleService scheduleService,
            ScheduleService solverService,
            TaskRepository taskRepository,
            PersonRepository personRepository) {
        this.personService = personService;
        this.taskService = taskService;
        this.scheduleService = scheduleService;
        this.solverService = solverService;
        this.taskRepository = taskRepository;
        this.personRepository = personRepository;
    }

    // ========================= PERSON =========================

    public Long createPerson(PersonRequestDTO person) {
        Person saved = personService.create(person);
        scheduleService.invalidate();
        return saved.getId();
    }

    public void updatePerson(PersonRequestDTO person) {
        personService.update(person.getId(), person);
        scheduleService.invalidate();
    }

    public void deletePerson(Long id) {
        personService.delete(id);
        scheduleService.invalidate();
    }

    public List<Person> listPersons() {
        return personService.findAll();
    }

    // ========================= TASK =========================

    public Long createTask(TaskRequestDTO task) {
        Task saved = taskService.create(task);
        scheduleService.invalidate();
        return saved.getId();
    }

    public void updateTask(TaskRequestDTO task) {
        taskService.update(task.getId(), task);
        scheduleService.invalidate();
    }

    public void deleteTask(Long id) {
        taskService.delete(id);
        scheduleService.invalidate();
    }

    public List<Task> listTasks() {
        return taskService.findAll();
    }

    // ========================= SCHEDULE =========================

    public Schedule generateSchedule(ScheduleRequestDTO request) {
        // 1️⃣ DTO → Schedule
        Schedule schedule = ScheduleMapper.toModel(request, taskRepository, personRepository);
        
        // 2️⃣ Cargar Schedule en memoria
        scheduleService.loadSchedule(schedule);
        
        // 3️⃣ Resolverlo con OptaPlanner
        Schedule solved = solverService.solve(schedule);

        return solved;
    }

    public Schedule getCurrentSchedule() {
        return scheduleService.getCurrentSchedule();
    }

    public void resetSchedule() {
        scheduleService.reset();
    }

    public boolean isScheduleInvalidated() {
        return scheduleService.isInvalidated();
    }
}
