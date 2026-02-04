package com.AgsCh.task_scheduler.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/functions")
public class FunctionsController {
    @GetMapping
    public String functions() {
        return "admin/functions";
    }
}