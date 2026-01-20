package com.AgsCh.task_scheduler.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.service.PersonStore;

@RestController
@RequestMapping("/api/persons")
public class PersonApiController {

    private final PersonStore store;

    public PersonApiController(PersonStore store) {
        this.store = store;
    }

    @PostMapping
    public UUID create(@RequestBody PersonRequestDTO person) {
        return store.save(person);
    }

    @GetMapping
    public Map<UUID, PersonRequestDTO> list() {
        return store.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        store.delete(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable UUID id, @RequestBody PersonRequestDTO person) {
        store.update(id, person);
    }
}
