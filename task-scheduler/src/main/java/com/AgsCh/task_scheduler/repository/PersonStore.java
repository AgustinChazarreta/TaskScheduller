package com.AgsCh.task_scheduler.repository;

import java.util.*;
import org.springframework.stereotype.Repository;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;

@Repository
public class PersonStore {

    private final Map<UUID, PersonRequestDTO> persons = new LinkedHashMap<>();

    public UUID save(PersonRequestDTO person) {
        UUID id = UUID.randomUUID();
        persons.put(id, person);
        return id;
    }

    public Map<UUID, PersonRequestDTO> findAll() {
        return Map.copyOf(persons);
    }

    public List<PersonRequestDTO> findByIds(List<UUID> ids) {
        return ids.stream()
                .map(persons::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public void clear() {
        persons.clear();
    }

    public void delete(UUID id) {
        persons.remove(id);
    }

    public void update(UUID id, PersonRequestDTO person) {
        persons.put(id, person);
    }
}
