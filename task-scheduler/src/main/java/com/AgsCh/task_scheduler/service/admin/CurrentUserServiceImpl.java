package com.AgsCh.task_scheduler.service.admin;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AgsCh.task_scheduler.model.AdminData;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.service.domain.CurrentUserService;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private static final Map<String, List<String>> ORDEN_MAP = Map.of(
            "ORDEN_I", List.of("Primeira"),
            "ORDEN_II", List.of("Segunda"));

    private final UserRepository userRepository;

    public CurrentUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCurrentUserOrdens() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsernameWithAdminData(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminData adminData = user.getAdminData();

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

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}