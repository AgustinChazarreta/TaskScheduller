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
import com.AgsCh.task_scheduler.service.admin.EmailService;

import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetController(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(String email, Model model) {

        User user = userRepository.findByUsername(email).orElse(null);

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

        String resetLink = "http://www.funcoescheznous.org/reset-password?token=" + token;

        emailService.sendPasswordResetEmail(email, resetLink);

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
        if (!isValidPassword(password)) {
            model.addAttribute("error",
                    "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y símbolo.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.delete(resetToken);

        return "redirect:/login?passwordResetSuccess";
    }

    private boolean isValidPassword(String password) {
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$");
    }
}