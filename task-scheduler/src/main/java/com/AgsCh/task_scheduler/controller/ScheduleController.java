package com.AgsCh.task_scheduler.controller;

import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.service.ScheduleService;
import com.AgsCh.task_scheduler.util.TestData;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    // TEST (borrar después)
    @GetMapping("/solve/test")
    public Schedule solveTest() {
        Schedule problem = TestData.buildSchedule();
        return scheduleService.solve(problem);
    }

    // REAL
    @PostMapping("/solve")
    public ScheduleResponseDTO solve(@RequestBody ScheduleRequestDTO request) {

        Schedule problem = ScheduleMapper.toModel(request);
        Schedule solution = scheduleService.solve(problem);

        return ScheduleMapper.toResponse(solution);
    }

}
