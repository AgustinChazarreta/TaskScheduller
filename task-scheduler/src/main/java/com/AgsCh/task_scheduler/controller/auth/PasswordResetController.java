package com.AgsCh.task_scheduler.controller.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.AgsCh.task_scheduler.model.PasswordResetToken;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.PasswordResetTokenRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(String username, Model model) {

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {

            model.addAttribute("message",
                    "Si el email existe, recibirás un enlace para restablecer tu contraseña.");

            return "forgot-password";
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("LINK DE RECUPERACION: " + resetLink);
        System.out.println("--------------------------------------------------------------------------------");

        model.addAttribute("message",
                "Si el email existe, recibirás un enlace para restablecer tu contraseña.");

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(String token, Model model) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "redirect:/login?errorToken";
        }

        model.addAttribute("token", token);

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(String token, String password, Model model) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "redirect:/login?errorToken";
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        tokenRepository.delete(resetToken);

        model.addAttribute("message", "Contraseña actualizada correctamente.");

        return "redirect:/login?passwordResetSuccess";
    }
}