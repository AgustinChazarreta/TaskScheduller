package com.AgsCh.task_scheduler.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('USER')")
public class UserWebController {

    @GetMapping("/user")
    public String userDashboard() {
        return "user/dashboard";
    }
}