package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.response.PersonCreatedResponseDTO;
import com.AgsCh.task_scheduler.dto.response.UserResponseDTO;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.UserService;
import com.AgsCh.task_scheduler.service.domain.PersonService;

@RestController
@RequestMapping("/api/webmaster/users")
@PreAuthorize("hasRole('WEBMASTER')")
public class WebmasterUserApiController {

    private final UserService userService;
    private final PersonService personService;

    public WebmasterUserApiController(UserService userService, PersonService personService) {
        this.userService = userService;
        this.personService = personService;
    }

    // ================= LISTAR TODOS LOS USUARIOS =================
    @GetMapping
    public List<UserResponseDTO> listAllUsers() {
        List<User> users = userService.getAllUsersWithAdminData();
        return users.stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    @GetMapping("/persons")
    public List<UserResponseDTO> listRoleUser() {
        return userService.getAllUsersPerson().stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    // ================= LISTAR USUARIOS POR CASA =================
    @GetMapping("/houses/{houseId}")
    public List<UserResponseDTO> listUsersByHouse(@PathVariable Long houseId) {
        return userService.getAllUsers().stream()
                .filter(u -> u.getHouse() != null && u.getHouse().getId().equals(houseId))
                .map(UserResponseDTO::new)
                .toList();
    }

    // ================= OBTENER USUARIO POR ID =================
    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {

        User user = userService.getUserById(id);

        return new UserResponseDTO(user);
    }

    // ================= CREAR USUARIO NORMAL =================
    @PostMapping
    public PersonCreatedResponseDTO createUser(@RequestParam Long houseId,
            @RequestBody PersonRequestDTO dto) {
        return personService.createForHouse(houseId, dto);
    }

    // ================= ACTUALIZAR USUARIO =================
    @PutMapping("/{id}")
    public ResponseEntity<PersonCreatedResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody PersonRequestDTO dto) {

        PersonCreatedResponseDTO updatedPerson = userService.updateUser(id, dto); // asegúrate que retorne personId
        return ResponseEntity.ok(updatedPerson);
    }

    // ================= ELIMINAR USUARIO =================
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PostMapping("/persons/{id}/profile-image")
    public ResponseEntity<String> uploadProfileImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        String url = personService.uploadProfileImage(id, file);

        return ResponseEntity.ok(url);
    }
}