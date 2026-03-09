package com.AgsCh.task_scheduler.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.request.PersonUnavailabilityDTO;
import com.AgsCh.task_scheduler.dto.response.PersonResponseDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.UserService;
import com.AgsCh.task_scheduler.service.domain.PersonService;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final PersonService personService;
    private final UserService userService;

    public UserController(PersonService personService, UserService userService) {
        this.personService = personService;
        this.userService = userService;
    }

    // Endpoint original para Person
    @GetMapping("/me")
    public PersonResponseDTO getMyProfile() {
        return personService.getMyProfile();
    }

    // Nuevo endpoint para incluir role y house
    @GetMapping("/me/details")
    public PersonResponseDTO getMyUserDetails() {
        User user = userService.getAuthenticatedUser(); // devuelve el User logueado
        PersonResponseDTO dto = personService.mapToResponseDTOSafe(user.getPerson()); // <--- usar mapToResponseDTO

        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        if (user.getHouse() != null) {
            dto.setHouseName(user.getHouse().getName());
        }
        if (user.getPerson().getGroup() != null) {
            dto.setGroupName(user.getPerson().getGroup().getName());
        }

        return dto;
    }

    @PutMapping("/me")
    public void updateMyProfile(@RequestBody PersonRequestDTO dto) {
        personService.updateMyProfile(dto);
    }

    @PostMapping("/profile-image")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        User user = userService.getAuthenticatedUser();
        try {
            String imageUrl = personService.uploadProfileImage(user.getPerson().getId(), file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al subir la imagen");
        }
    }

    @GetMapping("/me/unavailabilities")
    public List<PersonUnavailabilityDTO> getMyUnavailabilities() {
        User user = userService.getAuthenticatedUser();

        // mapear de entidad a DTO
        return user.getPerson().getUnavailabilities().stream()
                .map(u -> new PersonUnavailabilityDTO(
                        u.getStartDate(),
                        u.getEndDate(),
                        u.getReason()))
                .collect(Collectors.toList());
    }

    @GetMapping("/my-schedule")
    public ScheduleResponseDTO getMySchedule() {
        return personService.getMySchedule();
    }
}
