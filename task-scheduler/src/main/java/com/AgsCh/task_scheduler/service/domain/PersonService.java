package com.AgsCh.task_scheduler.service.domain;

import java.util.List;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;

@Service
public class PersonService {

    private final PersonRepository repository;
    private final AdminScheduleService scheduleService;

    public PersonService(PersonRepository repository,
            AdminScheduleService scheduleService) {
        this.repository = repository;
        this.scheduleService = scheduleService;
    }

    // CREATE
    public Person create(PersonRequestDTO dto) {
        Person person = new Person(
                dto.getFullName(),
                dto.getBirthDate(),
                dto.getWorkingDays());
        scheduleService.invalidate();
        return repository.save(person);
    }

    // READ
    public List<Person> findAll() {
        return repository.findAll();
    }

    public Person findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
    }

    // UPDATE
    public void update(Long id, PersonRequestDTO dto) {
        Person person = findById(id);

        person.setFullName(dto.getFullName());
        person.setNickName(dto.getNickName());
        person.setBirthDate(dto.getBirthDate());
        person.setWorkingDays(dto.getWorkingDays());
        person.setEmail(dto.getEmail());
        person.setEmailNotificationsEnabled(dto.isEmailNotificationsEnabled());
        person.setActive(dto.isActive());
        person.setEntryDate(dto.getEntryDate());
        person.setExitDate(dto.getExitDate());

        scheduleService.invalidate();
        repository.save(person);
    }
    
    // DELETE
    public void delete(Long id) {
        repository.deleteById(id);
        scheduleService.invalidate();
    }
}
