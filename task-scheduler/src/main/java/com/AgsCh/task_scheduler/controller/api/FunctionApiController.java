package com.AgsCh.task_scheduler.controller.api;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.FunctionRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionResponseDTO;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.model.WordTemplate;
import com.AgsCh.task_scheduler.service.admin.WordTemplateService;
import com.AgsCh.task_scheduler.service.domain.CurrentUserService;
import com.AgsCh.task_scheduler.service.domain.FunctionService;
import com.AgsCh.task_scheduler.util.word.WordParser;

@RestController
@RequestMapping("/api/functions")
public class FunctionApiController {

    private final FunctionService service;
    private final WordTemplateService wordTemplateService;
    private final CurrentUserService currentUserService;

    public FunctionApiController(
            FunctionService service,
            WordTemplateService wordTemplateService,
            CurrentUserService currentUserService) {

        this.service = service;
        this.wordTemplateService = wordTemplateService;
        this.currentUserService = currentUserService;
    }

    // -------- CREATE --------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void createFunctions(
            @RequestBody List<FunctionRequestDTO> functions,
            Authentication authentication) {

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
    public void update(
            @PathVariable Long id,
            @RequestBody FunctionRequestDTO dto) {

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
    public List<FunctionResponseDTO> parseFromWord(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        User user = currentUserService.getCurrentUser(authentication);

        // 🔥 REEMPLAZA TEMPLATE ANTERIOR
        wordTemplateService.saveTemplate(file, user.getHouse());

        List<FunctionRequestDTO> dtos = WordParser.parseFunctionsFromWord(file);

        return dtos.stream()
                .map(dto -> new FunctionResponseDTO(
                        null,
                        dto.getName(),
                        dto.isSequential(),
                        dto.getAssignedDays(),
                        dto.getRequiredPersons()))
                .toList();
    }

    // -------- GET PERSISTED WORD --------
    @GetMapping("/word-template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getWordTemplate(
            Authentication authentication) {

        User user = currentUserService.getCurrentUser(authentication);

        WordTemplate template = wordTemplateService.getTemplate(user.getHouse().getId());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + template.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(
                        template.getContentType()))
                .body(template.getData());
    }
}