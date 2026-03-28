package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.CreateWebmasterRequestDTO;
import com.AgsCh.task_scheduler.dto.response.CreatedWebmasterResponseDTO;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.UserService;
import com.AgsCh.task_scheduler.service.admin.WebmasterService;

@RestController
@RequestMapping("/api/webmaster/webmasters")
@PreAuthorize("hasRole('WEBMASTER')")
public class WebmasterApiController {

    private final UserService userService;
    private final WebmasterService webmasterService;

    public WebmasterApiController(UserService userService, WebmasterService webmasterService) {
        this.userService = userService;
        this.webmasterService = webmasterService;
    }

    // Obtener todos los webmasters
    @GetMapping
    public List<CreatedWebmasterResponseDTO> getWebmasters() {
        return userService.getAllWebmasters()
                .stream()
                .map(CreatedWebmasterResponseDTO::new)
                .toList();
    }

    // Crear webmaster
    @PostMapping
    public ResponseEntity<CreatedWebmasterResponseDTO> createWebmaster(
            @RequestBody CreateWebmasterRequestDTO request) {

        String temporaryPassword = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        User webmaster = userService.createWebmaster(
                request.getUsername(),
                request.getNombre(),
                temporaryPassword);

        return ResponseEntity.ok(new CreatedWebmasterResponseDTO(webmaster, temporaryPassword));
    }

    // Actualizar webmaster
    @PutMapping("/{id}")
    public ResponseEntity<CreatedWebmasterResponseDTO> updateWebmaster(
            @PathVariable Long id,
            @RequestBody CreateWebmasterRequestDTO request) {

        User updated = webmasterService.updateWebmaster(
                id,
                request.getUsername(),
                request.getNombre(),
                request.isActive());

        return ResponseEntity.ok(new CreatedWebmasterResponseDTO(updated));
    }

    // Eliminar webmaster
    @DeleteMapping("/{id}")
    public void deleteWebmaster(@PathVariable Long id) {
        webmasterService.deleteWebmaster(id);        
    }
}