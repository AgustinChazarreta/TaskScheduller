package com.AgsCh.task_scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/persons")
public class PersonController {
    @GetMapping
    public String persons(Model model) {
        model.addAttribute("active", "persons");
        model.addAttribute("title", "Personas");
        return "persons";
    }

}