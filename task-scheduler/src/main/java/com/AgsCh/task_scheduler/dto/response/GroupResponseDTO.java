package com.AgsCh.task_scheduler.dto.response;

import java.util.List;

public class GroupResponseDTO {

    private Long id;
    private String name;
    private List<PersonResponseDTO> persons;

    public GroupResponseDTO() {
    }

    public GroupResponseDTO(Long id, String name, List<PersonResponseDTO> persons) {
        this.id = id;
        this.name = name;
        this.persons = persons;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PersonResponseDTO> getPersons() {
        return persons;
    }
}