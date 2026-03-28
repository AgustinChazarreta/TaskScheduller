package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.AdminRegisterRequest;
import com.AgsCh.task_scheduler.dto.request.CreateAdminRequestDTO;
import com.AgsCh.task_scheduler.dto.response.AdminCreatedResponseDTO;
import com.AgsCh.task_scheduler.dto.response.AdminPendingResponseDTO;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.AdminService;
import com.AgsCh.task_scheduler.service.admin.UserService;

@RestController
@RequestMapping("/api/webmaster/admins")
@PreAuthorize("hasRole('WEBMASTER')")
public class WebmasterAdminApiController {

    private final UserService userService;
    private final AdminService adminService;

    public WebmasterAdminApiController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    // Devuelve todos los admins (sin temporaryPassword)
    @GetMapping
    public List<AdminCreatedResponseDTO> getAdmins() {
        return userService.getAllAdmins()
                .stream()
                .map(user -> new AdminCreatedResponseDTO(user)) // constructor sin temporaryPassword
                .toList();
    }

    @GetMapping("/houses/{houseId}")
    public List<AdminCreatedResponseDTO> getAdminsByHouse(@PathVariable Long houseId) {
        return userService.getAdminsByHouse(houseId)
                .stream()
                .map(AdminCreatedResponseDTO::new)
                .toList();
    }

    // Crear admin y devolver DTO completo con contraseña temporal
    @PostMapping("/houses/{houseId}")
    public ResponseEntity<AdminCreatedResponseDTO> createAdmin(
            @PathVariable Long houseId,
            @RequestBody AdminRegisterRequest request) {

        // Generar contraseña temporal
        String temporaryPassword = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        User admin = userService.createAdmin(
                houseId,
                request.getEmail(),
                temporaryPassword,
                request.getNombre(),
                request.getOrden(),
                request.getSedeResidencia(),
                request.getEncargado());

        AdminCreatedResponseDTO dto = new AdminCreatedResponseDTO(admin, temporaryPassword);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminCreatedResponseDTO> updateAdmin(
            @PathVariable Long id,
            @RequestBody CreateAdminRequestDTO request) {

        User updated = adminService.updateAdmin(
                id,
                request.getUsername(),
                request.isActive(),
                request.getHouseId(),
                request.getNombre(),
                request.getOrden(),
                request.getSedeResidencia(),
                request.getEncargado());

        return ResponseEntity.ok(new AdminCreatedResponseDTO(updated));

    }

    // Eliminar admin
    @DeleteMapping("/{id}")
    public void deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
    }

    @GetMapping("/pending")
    public List<AdminPendingResponseDTO> getPendingAdmins() {
        return userService.getPendingAdmins()
                .stream()
                .map(AdminPendingResponseDTO::new)
                .toList();
    }

    @PutMapping("/{id}/activate")
    public void completeAndActivateAdmin(
            @PathVariable Long id,
            @RequestParam Long houseId,
            @RequestParam String sede,
            @RequestParam String encargado) {

        adminService.completeAndActivateAdmin(id, houseId, sede, encargado);
    }
}