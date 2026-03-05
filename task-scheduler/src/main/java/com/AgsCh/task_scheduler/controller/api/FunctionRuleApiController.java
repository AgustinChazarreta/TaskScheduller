package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.request.FunctionRuleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionRuleResponseDTO;
import com.AgsCh.task_scheduler.service.admin.FunctionRuleService;

@RestController
@RequestMapping("/api/function-rules")
@PreAuthorize("hasRole('ADMIN')")
public class FunctionRuleApiController {

    private final FunctionRuleService service;

    public FunctionRuleApiController(FunctionRuleService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    public void create(@RequestBody FunctionRuleRequestDTO dto,
            Authentication auth) {
        service.create(dto, auth.getName());
    }

    // READ
    @GetMapping
    public List<FunctionRuleResponseDTO> list(Authentication auth) {
        return service.findByUser(auth.getName());
    }

    // UPDATE
    @PutMapping("/{id}")
    public void update(@PathVariable Long id,
            @RequestBody FunctionRuleRequestDTO dto,
            Authentication auth) {
        service.update(id, dto, auth.getName());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth.getName());
    }
}