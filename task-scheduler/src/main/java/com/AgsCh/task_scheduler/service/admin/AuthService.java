package com.AgsCh.task_scheduler.service.admin;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.request.AdminRegisterRequest;
import com.AgsCh.task_scheduler.model.*;
import com.AgsCh.task_scheduler.repository.*;

@Service
public class AuthService {

    private final PendingAdminRegistrationRepository pendingRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(PendingAdminRegistrationRepository pendingRepo,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.pendingRepo = pendingRepo;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // 🟡 REGISTRO (NO crea User)
    public void registerAdmin(AdminRegisterRequest request) {

        if (userRepository.existsByUsername(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (pendingRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya hay un registro pendiente con este email");
        }

        PendingAdminRegistration pending = new PendingAdminRegistration();

        pending.setNombre(request.getNombre());
        pending.setEmail(request.getEmail());
        pending.setPassword(passwordEncoder.encode(request.getPassword()));
        pending.setOrden(request.getOrden());
        pending.setSedeResidencia(request.getSedeResidencia());
        pending.setEncargado(request.getEncargado());

        String token = UUID.randomUUID().toString();
        pending.setToken(token);
        pending.setExpiryDate(LocalDateTime.now().plusHours(24));

        pendingRepo.save(pending);

        String link = "https://localhost:8080/auth/verify?token=" + token;
        emailService.sendVerificationEmail(request.getEmail(), link);
    }

    // 🟢 CONFIRMACIÓN (CREA USER)
    public void confirmAdminRegistration(String token) {

        PendingAdminRegistration pending = pendingRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (pending.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        User user = new User();
        user.setUsername(pending.getEmail());
        user.setPassword(pending.getPassword());
        user.setRole(Role.ADMIN);
        user.setActive(false); // 🔴 importante
        user.setHouse(null); // 🔴 importante
        user.setPasswordTemporary(true);

        AdminData adminData = new AdminData();
        adminData.setNombre(pending.getNombre());
        adminData.setOrden(pending.getOrden());
        adminData.setSedeResidencia(pending.getSedeResidencia());
        adminData.setEncargado(pending.getEncargado());

        user.setAdminData(adminData);

        userRepository.save(user);

        pendingRepo.delete(pending);
    }
}