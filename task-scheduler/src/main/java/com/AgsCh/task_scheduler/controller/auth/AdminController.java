package com.AgsCh.task_scheduler.controller.auth;

import org.springframework.web.bind.annotation.RestController;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.response.PersonCreatedResponseDTO;
import com.AgsCh.task_scheduler.service.domain.PersonService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/admin")
@PreAuthorize("@authz.canAccessAdmin(authentication)")
public class AdminController {

    private final PersonService personService;

    public AdminController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping("/persons")
    public PersonCreatedResponseDTO createPerson(@RequestBody PersonRequestDTO dto) {
        return personService.create(dto);
    }

}
