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
    public String create(@ModelAttribute("ruleDto") FunctionRuleRequestDTO dto, Model model) {
        try {
            functionRuleService.create(dto, getCurrentUsername());
            return "redirect:/admin/function-rules";
        } catch (IllegalArgumentException e) {
            // Capturamos la excepción y mostramos el mensaje en el modal
            List<FunctionRuleResponseDTO> rules = functionRuleService.findByUser(getCurrentUsername());
            model.addAttribute("rules", rules);
            model.addAttribute("functions", functionService.findAll());
            model.addAttribute("ruleDto", dto);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/function-rules"; // vuelve al mismo template
        }
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("ruleDto") FunctionRuleRequestDTO dto, Model model) {
        try {
            functionRuleService.update(id, dto, getCurrentUsername());
            return "redirect:/admin/function-rules";
        } catch (IllegalArgumentException e) {
            // igual que en create, mostramos error en el modal de edición
            List<FunctionRuleResponseDTO> rules = functionRuleService.findByUser(getCurrentUsername());
            model.addAttribute("rules", rules);
            model.addAttribute("functions", functionService.findAll());
            model.addAttribute("ruleDto", dto);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/function-rules";
        }
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