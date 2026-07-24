package com.AgsCh.task_scheduler.service.admin;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AgsCh.task_scheduler.model.AdminData;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.service.domain.CurrentUserService;
import com.AgsCh.task_scheduler.session.AdminSession;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private static final Map<String, List<String>> ORDEN_MAP = Map.of(
            "ORDEN_I", List.of("Primeira"),
            "ORDEN_II", List.of("Segunda"));

    private final UserRepository userRepository;
    private final AdminSession adminSession;

    public CurrentUserServiceImpl(UserRepository userRepository, AdminSession adminSession) {
        this.userRepository = userRepository;
        this.adminSession = adminSession;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCurrentUserOrdens() {

        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow();

        AdminData adminData = null;

        // ===============================
        // ADMIN NORMAL
        // ===============================
        if (user.getRole() == Role.ADMIN) {

            adminData = user.getAdminData();

        }

        // ===============================
        // WEBMASTER IMPERSONANDO
        // ===============================
        else if (user.getRole() == Role.WEBMASTER
                && adminSession.isImpersonating()) {

            List<User> admins = userRepository.findByHouseIdAndRole(
                    adminSession.getHouseId(),
                    Role.ADMIN);

            if (admins.isEmpty()) {
                throw new RuntimeException(
                        "No existe ADMIN para esta House");
            }

            User admin = admins.get(0);

            adminData = admin.getAdminData();
        }

        if (adminData == null) {
            throw new RuntimeException("AdminData missing");
        }

        List<String> ordens = ORDEN_MAP.get(adminData.getOrden());

        if (ordens == null) {
            throw new RuntimeException("Invalid orden");
        }

        return ordens;
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }
}