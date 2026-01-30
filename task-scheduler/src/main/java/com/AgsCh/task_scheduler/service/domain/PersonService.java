package com.AgsCh.task_scheduler.service.domain;
import java.util.List;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.repository.PersonRepository;

@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    // CREATE
    public Person create(PersonRequestDTO dto) {
        Person person = new Person(
                dto.getName(),
                dto.getCategory(),
                dto.getBirthDate(),
                dto.getAvailableDays());
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

        person.setName(dto.getName());
        person.setCategory(dto.getCategory());
        person.setBirthDate(dto.getBirthDate());
        person.setAvailableDays(dto.getAvailableDays());

        repository.save(person);
    }

    // DELETE
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
