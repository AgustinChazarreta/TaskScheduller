package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.GroupRequestDTO;
import com.AgsCh.task_scheduler.dto.response.GroupResponseDTO;
import com.AgsCh.task_scheduler.service.admin.GroupService;

@RestController
@RequestMapping("/api/groups")
public class GroupApiController {

    private final GroupService service;

    public GroupApiController(GroupService service) {
        this.service = service;
    }

    // 🔹 Listar todos los grupos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WEBMASTER')")
    public List<GroupResponseDTO> list() {
        return service.findAll();
    }

    // 🔹 Crear un nuevo grupo
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public GroupResponseDTO create(@RequestBody GroupRequestDTO dto) {
        return service.create(dto);
    }

    // 🔹 Actualizar un grupo existente
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GroupResponseDTO update(@PathVariable Long id,
            @RequestBody GroupRequestDTO dto) {
        return service.update(id, dto);
    }

    // 🔹 Eliminar un grupo
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}