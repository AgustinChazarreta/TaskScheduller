package com.AgsCh.task_scheduler.controller;


import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.service.ScheduleService;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/solve")
public ScheduleResponseDTO solve(@RequestBody ScheduleRequestDTO request) {

    //System.out.println("1️⃣ request recibido");
    //System.out.println("Persons: " + request.getPersons());
    //System.out.println("Tasks: " + request.getTasks());

    Schedule schedule = ScheduleMapper.toModel(request);
    //System.out.println("2️⃣ mapper OK");

    Schedule solved = scheduleService.solve(schedule);
    //System.out.println("3️⃣ solver OK");

    ScheduleResponseDTO response = ScheduleMapper.toResponse(solved);
    //System.out.println("4️⃣ response OK");

    return response;
}

}
