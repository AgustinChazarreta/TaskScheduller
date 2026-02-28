package com.AgsCh.task_scheduler.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.response.PersonCreatedResponseDTO;
import com.AgsCh.task_scheduler.dto.response.PersonResponseDTO;
import com.AgsCh.task_scheduler.service.domain.PersonService;

@RestController
@RequestMapping("/api/persons")
public class PersonApiController {

    private final PersonService service;

    public PersonApiController(PersonService service) {
        this.service = service;
    }

    // -------- CREATE --------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PersonCreatedResponseDTO create(@RequestBody PersonRequestDTO dto) {
        return service.create(dto);
    }

    // -------- READ ALL --------
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<PersonResponseDTO> list() {
        return service.findAll().stream()
                .map(service::mapToResponseDTOSafe) // usamos el DTO seguro que incluye unavailabilities
                .collect(Collectors.toList());
    }

    // -------- UPDATE --------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable Long id,
            @RequestBody PersonRequestDTO dto) {
        service.update(id, dto);
    }

    // -------- DELETE --------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // -------- UPLOAD PROFILE IMAGE --------
    @PostMapping("/{id}/profile-image")
    public ResponseEntity<String> uploadProfileImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        String url = service.uploadProfileImage(id, file);
        return ResponseEntity.ok(url);
    }
}