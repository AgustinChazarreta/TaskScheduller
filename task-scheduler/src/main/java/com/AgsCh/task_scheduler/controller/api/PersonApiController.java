package com.AgsCh.task_scheduler.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.response.PersonResponseDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.repository.PersonRepository;

@RestController
@RequestMapping("/api/persons")
public class PersonApiController {

    private final PersonRepository repository;

    public PersonApiController(PersonRepository repository) {
        this.repository = repository;
    }

    // -------- CREATE --------
    @PostMapping
    public Long create(@RequestBody PersonRequestDTO dto) {
        Person person = new Person(
            dto.getName(),
            dto.getCategory(),
            dto.getBirthDate(),
            dto.getAvailableDays()
        );

        return repository.save(person).getId();
    }

    // -------- READ --------
    @GetMapping
    public List<PersonResponseDTO> list() {
        return repository.findAll().stream()
            .map(p -> new PersonResponseDTO(
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getBirthDate(),
                p.getAvailableDays()
            ))
            .collect(Collectors.toList());
    }

    // -------- UPDATE --------
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody PersonRequestDTO dto) {
        Person person = repository.findById(id)
            .orElseThrow();

        person.setAvailableDays(dto.getAvailableDays());
        person.setCategory(dto.getCategory());
        person.setName(dto.getName());
        person.setBirthDate(dto.getBirthDate());

        repository.save(person);
    }

    // -------- DELETE --------
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
