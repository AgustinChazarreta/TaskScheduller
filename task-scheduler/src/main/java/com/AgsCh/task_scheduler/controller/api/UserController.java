package com.AgsCh.task_scheduler.controller.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.response.PersonResponseDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.service.domain.PersonService;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final PersonService personService;

    public UserController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/me")
    public PersonResponseDTO getMyProfile() {
        return personService.getMyProfile();
    }

    @PutMapping("/me")
    public void updateMyProfile(@RequestBody PersonRequestDTO dto) {
        personService.updateMyProfile(dto);
    }

    @GetMapping("/my-schedule")
    public ScheduleResponseDTO getMySchedule() {
        return personService.getMySchedule();
    }
}
