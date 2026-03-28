package com.AgsCh.task_scheduler.service.admin;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.UserRepository;

@Service
public class WebmasterService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public WebmasterService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User updateWebmaster(Long id, String username, String nombre, boolean active) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Webmaster no encontrado"));

        boolean wasInactive = !user.isActive(); // guardamos si antes estaba inactivo

        user.setUsername(username);
        user.setActive(active);

        if (user.getAdminData() != null) {
            user.getAdminData().setNombre(nombre);
        }

        User updatedWebmaster = userRepository.save(user);

        // 🔥 Enviar mail si se activó la cuenta
        if (wasInactive && active) {
            // suponiendo que tenés un EmailService similar al que usaste para crear
            emailService.sendWebmasterActivationEmail(
                    user.getUsername(),
                    user.getAdminData().getNombre());
        }

        return updatedWebmaster;
    }

    public void deleteWebmaster(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Webmaster no encontrado"));

        if (user.getRole() != Role.WEBMASTER) {
            throw new RuntimeException("El usuario no es webmaster");
        }

        // Obtener el usuario que está intentando eliminar
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName(); // nombre de usuario logueado

        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException("No puedes eliminar tu propia cuenta de Webmaster");
        }

        long activeWebmasters = userRepository.countByRoleAndActiveTrue(Role.WEBMASTER);

        if (activeWebmasters <= 1) {
            throw new BusinessException("No se puede eliminar el último Webmaster activo");
        }

        userRepository.delete(user);
    }
}