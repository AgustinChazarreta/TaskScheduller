package com.AgsCh.task_scheduler.service.domain;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.PersonFunction;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;

import jakarta.transaction.Transactional;

@Service
public class PersonService {

    private final PersonRepository repository;
    private final FunctionRepository functionRepository;
    private final AdminScheduleService scheduleService;

    public PersonService(
            PersonRepository repository,
            FunctionRepository functionRepository,
            AdminScheduleService scheduleService) {

        this.repository = repository;
        this.functionRepository = functionRepository;
        this.scheduleService = scheduleService;
    }

    // -------- CREATE --------
    @Transactional
    public Person create(PersonRequestDTO dto) {

        Person person = new Person(
                dto.getFullName(),
                dto.getNickName(),
                dto.getBirthDate(),
                dto.getEmail(),
                dto.isEmailNotificationsEnabled(),
                dto.isActive(),
                dto.getEntryDate(),
                dto.getExitDate(),
                dto.getWorkingDays()
        );

        if (dto.getFunctionIds() != null && !dto.getFunctionIds().isEmpty()) {
            List<Function> functions =
                    functionRepository.findAllById(dto.getFunctionIds());

            for (Function f : functions) {
                person.addPersonFunction(new PersonFunction(person, f));
            }
        }

        scheduleService.invalidate();
        return repository.save(person);
    }

    // -------- READ --------
    public List<Person> findAll() {
        return repository.findAll();
    }

    public Person findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
    }

    // -------- UPDATE --------
    @Transactional
    public void update(Long id, PersonRequestDTO dto) {

        Person person = findById(id);

        person.setFullName(dto.getFullName());
        person.setNickName(dto.getNickName());
        person.setBirthDate(dto.getBirthDate());
        person.setEmail(dto.getEmail());
        person.setEmailNotificationsEnabled(dto.isEmailNotificationsEnabled());
        person.setActive(dto.isActive());
        person.setEntryDate(dto.getEntryDate());
        person.setExitDate(dto.getExitDate());
        person.setWorkingDays(dto.getWorkingDays());

        // ---- FUNCIONES (sync real con DELETE) ----
        Set<Long> newFunctionIds =
                dto.getFunctionIds() != null ? dto.getFunctionIds() : Set.of();

        //eliminar relaciones que ya no estén
        person.getPersonFunctions().removeIf(pf ->
                !newFunctionIds.contains(pf.getFunction().getId())
        );

        //agregar nuevas relaciones
        for (Long functionId : newFunctionIds) {

            boolean exists = person.getPersonFunctions().stream()
                    .anyMatch(pf -> pf.getFunction().getId().equals(functionId));

            if (!exists) {
                Function f = functionRepository.findById(functionId)
                        .orElseThrow(() -> new RuntimeException("Function not found"));

                person.addPersonFunction(new PersonFunction(person, f));
            }
        }

        scheduleService.invalidate();
    }

    // -------- DELETE --------
    public void delete(Long id) {
        repository.deleteById(id);
        scheduleService.invalidate();
    }
}
