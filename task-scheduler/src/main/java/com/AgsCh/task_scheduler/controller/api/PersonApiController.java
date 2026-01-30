package com.AgsCh.task_scheduler.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.response.PersonResponseDTO;
import com.AgsCh.task_scheduler.service.domain.PersonService;

@RestController
@RequestMapping("/api/persons")
public class PersonApiController {

    private final PersonService service;

    public PersonApiController(PersonService service) {
        this.service = service;
    }

    // -------- CREATE --------
    @PostMapping
    public Long create(@RequestBody PersonRequestDTO dto) {
        return service.create(dto).getId();
    }

    // -------- READ --------
    @GetMapping
    public List<PersonResponseDTO> list() {
        return service.findAll().stream()
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
        service.update(id, dto);
    }

    // -------- DELETE --------
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
