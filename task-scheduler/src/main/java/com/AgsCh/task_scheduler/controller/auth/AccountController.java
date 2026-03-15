package com.AgsCh.task_scheduler.controller.auth;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.AgsCh.task_scheduler.service.admin.UserService;

@Controller
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/change-email")
    public String changeEmailPage(Model model) {

        var currentUser = userService.getAuthenticatedUser();

        model.addAttribute("currentEmail", currentUser.getUsername());

        return "change-email";
    }

    @PostMapping("/change-email")
    public String changeEmail(
            @RequestParam String email,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        var currentUser = userService.getAuthenticatedUser();

        if (email == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "El email no puede estar vacío");

            return "redirect:/change-email";
        }

        email = email.trim();

        // validar vacío
        if (email.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "El email no puede estar vacío");

            return "redirect:/change-email";
        }

        // validar formato email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "El formato del email no es válido");

            return "redirect:/change-email";
        }

        // evitar usar el mismo email
        if (email.equalsIgnoreCase(currentUser.getUsername())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "El nuevo email no puede ser igual al actual");

            return "redirect:/change-email";
        }

        // evitar duplicados
        if (userService.emailExists(email)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Ese email ya está registrado");

            return "redirect:/change-email";
        }

        // cambio de email
        userService.changeMyEmail(email);

        // cerrar sesión por seguridad
        request.getSession().invalidate();

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Email actualizado correctamente. Iniciá sesión nuevamente.");

        return "redirect:/login";
    }
}