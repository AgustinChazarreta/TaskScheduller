package com.AgsCh.task_scheduler.statistics.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.AdminService;
import com.AgsCh.task_scheduler.statistics.dto.FunctionStatsDTO;
import com.AgsCh.task_scheduler.statistics.dto.HouseStatisticsDTO;
import com.AgsCh.task_scheduler.statistics.dto.MonthlyStatsDTO;
import com.AgsCh.task_scheduler.statistics.dto.PersonStatisticsDTO;
import com.AgsCh.task_scheduler.statistics.service.HouseStatisticsService;

@Controller
@RequestMapping("/admin/statistics")
@PreAuthorize("@authz.canAccessAdmin(authentication)")
public class AdminStatisticsController {

        private final HouseStatisticsService service;
        private final AdminService adminService;

        public AdminStatisticsController(HouseStatisticsService service, AdminService adminService) {
                this.service = service;
                this.adminService = adminService;
        }

        @GetMapping
        public String getStatistics(Authentication authentication, Model model) {

                if (authentication == null) {
                        throw new RuntimeException("Usuario no autenticado");
                }

                String username = authentication.getName();
                User user = adminService.findByUsername(username);

                if (user == null) {
                        throw new RuntimeException("Usuario no encontrado");
                }

                if (user.getHouse() == null) {
                        throw new RuntimeException("El usuario no tiene House asignada");
                }

                Long houseId = user.getHouse().getId();

                HouseStatisticsDTO stats = service.buildHouseStatistics(houseId);
                model.addAttribute("stats", stats);

                model.addAttribute("personLabels",
                                stats.getPeopleStats().stream()
                                                .map(PersonStatisticsDTO::getFullName)
                                                .toList());

                model.addAttribute("personValues",
                                stats.getPeopleStats().stream()
                                                .map(PersonStatisticsDTO::getTotalAssignments)
                                                .toList());

                model.addAttribute("functionLabels",
                                stats.getFunctionStats().stream()
                                                .map(FunctionStatsDTO::getFunctionName)
                                                .toList());

                model.addAttribute("functionValues",
                                stats.getFunctionStats().stream()
                                                .map(FunctionStatsDTO::getCount)
                                                .toList());

                model.addAttribute("monthLabels",
                                stats.getMonthlyStats().stream()
                                                .map(MonthlyStatsDTO::getMonth)
                                                .toList());

                model.addAttribute("monthValues",
                                stats.getMonthlyStats().stream()
                                                .map(MonthlyStatsDTO::getCount)
                                                .toList());

                return "admin/statistics";
        }
}