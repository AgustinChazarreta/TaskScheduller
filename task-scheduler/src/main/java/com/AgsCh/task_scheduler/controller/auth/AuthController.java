package com.AgsCh.task_scheduler.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // nombre de la plantilla Thymeleaf: login.html
    }
}
