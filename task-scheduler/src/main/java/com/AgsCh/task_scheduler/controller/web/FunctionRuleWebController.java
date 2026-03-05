package com.AgsCh.task_scheduler.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.FunctionRuleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionRuleResponseDTO;
import com.AgsCh.task_scheduler.service.admin.FunctionRuleService;
import com.AgsCh.task_scheduler.service.domain.FunctionService;

import java.util.List;

@Controller
@RequestMapping("/admin/function-rules")
public class FunctionRuleWebController {

    private final FunctionRuleService functionRuleService;
    private final FunctionService functionService;

    public FunctionRuleWebController(FunctionRuleService functionRuleService,
            FunctionService functionService) {
        this.functionRuleService = functionRuleService;
        this.functionService = functionService;
    }

    @GetMapping
    public String list(Model model) {
        List<FunctionRuleResponseDTO> rules = functionRuleService.findByUser(getCurrentUsername());
        model.addAttribute("rules", rules);
        model.addAttribute("functions", functionService.findAll());
        model.addAttribute("ruleDto", new FunctionRuleRequestDTO());
        return "admin/function-rules";
    }

    @PostMapping
    public String create(@ModelAttribute("ruleDto") FunctionRuleRequestDTO dto) {
        functionRuleService.create(dto, getCurrentUsername());
        return "redirect:/admin/function-rules";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("ruleDto") FunctionRuleRequestDTO dto) {
        functionRuleService.update(id, dto, getCurrentUsername());
        return "redirect:/admin/function-rules";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        functionRuleService.delete(id, getCurrentUsername());
        return "redirect:/admin/function-rules";
    }

    private String getCurrentUsername() {
        return org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
    }
}