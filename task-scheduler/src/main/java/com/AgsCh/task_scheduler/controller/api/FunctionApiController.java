package com.AgsCh.task_scheduler.controller.api;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.FunctionRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionResponseDTO;
import com.AgsCh.task_scheduler.service.domain.FunctionService;

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
                        f.getAssignedDays()))
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
    public Map<String, Set<DayOfWeek>> parseFromWord(
            @RequestParam("file") MultipartFile file) {
        return service.parseTasksFromWord(file);
    }

}
