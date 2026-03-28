package com.AgsCh.task_scheduler.controller.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.AgsCh.task_scheduler.repository.UserRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("nombreUsuario")
    public String nombreUsuario(Authentication authentication) {
        if (authentication == null)
            return "";

        return userRepository.findByUsername(authentication.getName())
                .map(user -> {
                    if (user.getAdminData() != null && user.getAdminData().getNombre() != null) {
                        return user.getAdminData().getNombre();
                    } else {
                        return user.getUsername(); // fallback al email si no hay nombre
                    }
                })
                .orElse("");
    }
}