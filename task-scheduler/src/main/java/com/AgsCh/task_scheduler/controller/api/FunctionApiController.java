package com.AgsCh.task_scheduler.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.FunctionRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionResponseDTO;
import com.AgsCh.task_scheduler.service.domain.FunctionService;
import com.AgsCh.task_scheduler.util.word.WordParser;

@RestController
@RequestMapping("/api/functions")
public class FunctionApiController {

    private final FunctionService service;

    public FunctionApiController(FunctionService service) {
        this.service = service;
    }

    // -------- CREATE --------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void createFunctions(@RequestBody List<FunctionRequestDTO> functions, Authentication authentication) {
        service.createFunctions(functions, authentication.getName());
    }

    // -------- READ --------
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<FunctionResponseDTO> list() {

        return service.findAll()
                .stream()
                .map(f -> new FunctionResponseDTO(
                        f.getId(),
                        f.getName(),
                        f.isSequential(),
                        f.getAssignedDays(),
                        f.getRequiredPersons()))
                .collect(Collectors.toList());
    }

    // -------- UPDATE --------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable Long id, @RequestBody FunctionRequestDTO dto) {
        service.update(id, dto);
    }

    // -------- DELETE --------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // -------- LOAD WORD --------
    @PostMapping(value = "/from-word", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FunctionResponseDTO> parseFromWord(@RequestParam("file") MultipartFile file) {
        List<FunctionRequestDTO> dtos = WordParser.parseFunctionsFromWord(file);

        // Convertir a DTO
        List<FunctionResponseDTO> response = dtos.stream()
                .map(dto -> new FunctionResponseDTO(
                        null, // id todavía no existe
                        dto.getName(),
                        dto.isSequential(),
                        dto.getAssignedDays(),
                        dto.getRequiredPersons()))
                .toList();

        return response;
    }

}
