package com.AgsCh.task_scheduler.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.AgsCh.task_scheduler.service.admin.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PasswordController {

    private final UserService userService;

    public PasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Las contraseñas no coinciden");

            return "redirect:/change-password";
        }

        if (!isValidPassword(password)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y símbolo.");

            return "redirect:/change-password";
        }

        userService.changeMyPassword(password);

        // cerrar sesión
        request.getSession().invalidate();

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Contraseña actualizada correctamente. Iniciá sesión nuevamente.");

        return "redirect:/login";
    }

    private boolean isValidPassword(String password) {

        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$");
    }
}