package com.AgsCh.task_scheduler.statistics.controller;

import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.statistics.dto.PersonStatisticsDTO;
import com.AgsCh.task_scheduler.statistics.service.PersonStatisticsService;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final PersonStatisticsService service;

    public StatisticsController(PersonStatisticsService service) {
        this.service = service;
    }

    @GetMapping("/person/{personId}")
    public PersonStatisticsDTO getPersonStats(@PathVariable Long personId) {
        return service.getStatistics(personId);
    }
}