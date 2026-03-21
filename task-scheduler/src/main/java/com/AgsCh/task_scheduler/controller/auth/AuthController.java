package com.AgsCh.task_scheduler.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.AdminRegisterRequest;
import com.AgsCh.task_scheduler.service.admin.AuthService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminRegisterRequest request) {
        authService.registerAdmin(request);
        return ResponseEntity.ok("Revisá tu email para verificar la cuenta");
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token) {
        authService.confirmAdminRegistration(token);
        return "redirect:/login?verified=true";
    }
}