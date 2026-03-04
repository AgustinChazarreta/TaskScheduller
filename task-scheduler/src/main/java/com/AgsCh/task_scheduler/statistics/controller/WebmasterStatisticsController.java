package com.AgsCh.task_scheduler.statistics.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.statistics.dto.MonthlyStatsDTO;
import com.AgsCh.task_scheduler.statistics.dto.SystemStatisticsDTO;
import com.AgsCh.task_scheduler.statistics.service.WebmasterStatisticsService;
import com.AgsCh.task_scheduler.service.admin.HouseService;

@Controller
@RequestMapping("/webmaster/statistics")
public class WebmasterStatisticsController {

    private final WebmasterStatisticsService service;
    private final HouseService houseService;

    public WebmasterStatisticsController(WebmasterStatisticsService service,
            HouseService houseService) {
        this.service = service;
        this.houseService = houseService;
    }

    // ================= PAGE LOAD =================

    @GetMapping
    public String getStatisticsPage(Model model) {

        // 🔹 GLOBAL STATS (houseId = null)
        SystemStatisticsDTO stats = service.getStatistics(null);
        model.addAttribute("stats", stats);

        // 🔹 Houses para dropdown
        List<House> houses = houseService.getAllHouses();
        model.addAttribute("houses", houses);

        // 🔹 Person chart
        model.addAttribute("personLabels",
                stats.getPersonStats().keySet().stream().toList());

        model.addAttribute("personValues",
                stats.getPersonStats().values().stream().toList());

        // 🔹 Function chart
        model.addAttribute("functionLabels",
                stats.getFunctionStats().keySet().stream().toList());

        model.addAttribute("functionValues",
                stats.getFunctionStats().values().stream().toList());

        // 🔹 Monthly chart
        List<MonthlyStatsDTO> monthly = stats.getMonthlyStats();

        model.addAttribute("monthLabels",
                monthly.stream()
                        .map(MonthlyStatsDTO::getMonth)
                        .collect(Collectors.toList()));

        model.addAttribute("monthValues",
                monthly.stream()
                        .map(MonthlyStatsDTO::getCount)
                        .collect(Collectors.toList()));

        return "webmaster/statistics";
    }

    // ================= AJAX FILTER =================

    @GetMapping("/filter")
    @ResponseBody
    public SystemStatisticsDTO getStatsByHouse(
            @RequestParam(required = false) Long houseId) {

        return service.getStatistics(houseId);
    }
}