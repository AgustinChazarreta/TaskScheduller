package com.AgsCh.task_scheduler.controller.api;

import com.AgsCh.task_scheduler.dto.external.ExternalPersonDTO;
import com.AgsCh.task_scheduler.dto.external.ExternalPersonPreviewDTO;
import com.AgsCh.task_scheduler.service.admin.ExternalPersonImportService;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/external-persons")
public class ExternalPersonController {

    private final ExternalPersonImportService service;

    public ExternalPersonController(ExternalPersonImportService service) {
        this.service = service;
    }

    // 🔎 1. candidatos externos
    @GetMapping("/search")
    public List<ExternalPersonDTO> search(@RequestParam String name) {

        if (name == null || name.trim().length() < 2) {
            return List.of();
        }

        return service.search(name.trim());
    }

    // 👀 2. preview (opcional pero MUY útil)
    @GetMapping("/preview")
    public List<ExternalPersonPreviewDTO> preview(@RequestParam String name) {

        return service.search(name).stream()
                .map(dto -> new ExternalPersonPreviewDTO(
                        dto.getFullName(),
                        dto.getEmail(),
                        dto.getOrden(),
                        dto.getBirthDate(),
                        dto.getPhoto() != null
                                ? Base64.getEncoder().encodeToString(dto.getPhoto())
                                : null))
                .toList();
    }
}