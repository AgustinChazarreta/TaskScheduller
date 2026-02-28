package com.AgsCh.task_scheduler.controller.web;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.service.admin.AdminService;
import com.AgsCh.task_scheduler.service.admin.HouseService;
import com.AgsCh.task_scheduler.service.admin.UserService;
import com.AgsCh.task_scheduler.service.admin.WebmasterScheduleService;
import com.AgsCh.task_scheduler.service.domain.PersonService;

@Controller
@RequestMapping("/webmaster")
@PreAuthorize("hasRole('WEBMASTER')")
public class WebmasterWebController {

    private final HouseService houseService;
    private final UserService userService;
    private final PersonService personService;
    private final AdminService adminService;
    private final WebmasterScheduleService webmasterScheduleService;

    public WebmasterWebController(
            HouseService houseService,
            UserService userService,
            AdminService adminService,
            PersonService personService,
            WebmasterScheduleService webmasterScheduleService) {

        this.houseService = houseService;
        this.userService = userService;
        this.adminService = adminService;
        this.personService = personService;
        this.webmasterScheduleService = webmasterScheduleService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        var houses = houseService.getAllHouses();
        model.addAttribute("houses", houses);
        model.addAttribute("totalHouses", houses.size());
        model.addAttribute("totalUsers", userService.getAllUsersPerson().size());
        model.addAttribute("totalAdmins", userService.getAllAdmins().size());

        return "webmaster/dashboard";
    }

    @GetMapping("/houses")
    public String houses(Model model) {
        model.addAttribute("houses", houseService.getAllHouses());
        return "webmaster/houses";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("houses", houseService.getAllHouses());
        return "webmaster/users";
    }

    @PostMapping("/users/create-admin")
    public String createAdmin(
            @RequestParam Long houseId,
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        try {
            userService.createAdmin(houseId, username, password);
            redirectAttributes.addFlashAttribute("successMessage", "Admin creado correctamente");

        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/webmaster/users";
    }

    @PostMapping("/users/create-user")
    public String createUserPerson(
            @RequestParam Long houseId,
            @RequestParam String personFullName,
            @RequestParam(required = false) String personNickname,
            @RequestParam LocalDate personBirthDate,
            @RequestParam String personEmail,
            @RequestParam(defaultValue = "false") boolean receiveNotifications,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam LocalDate personEntryDate,
            @RequestParam(required = false) LocalDate personExitDate,
            @RequestParam(required = false) Set<DayOfWeek> personDays,
            @RequestParam(required = false) Set<Long> personFunctions,
            @RequestParam(required = false) MultipartFile personPhoto,
            RedirectAttributes redirectAttributes) {

        // Construimos el DTO
        PersonRequestDTO dto = new PersonRequestDTO();
        dto.setFullName(personFullName);
        dto.setNickName(personNickname);
        dto.setBirthDate(personBirthDate);
        dto.setEmail(personEmail);
        dto.setEmailNotificationsEnabled(receiveNotifications);
        dto.setActive(active);
        dto.setEntryDate(personEntryDate);
        dto.setExitDate(personExitDate);
        dto.setWorkingDays(personDays);
        dto.setFunctionIds(personFunctions);

        // 🔥 Creamos persona + user
        var result = personService.createForHouse(houseId, dto);

        // 🔥 Si vino imagen → la subimos reutilizando tu lógica existente
        if (personPhoto != null && !personPhoto.isEmpty()) {
            personService.uploadProfileImage(result.getPersonId(), personPhoto);
        }

        // Flash attributes
        redirectAttributes.addFlashAttribute("successMessage", "Usuario creado correctamente");
        redirectAttributes.addFlashAttribute("createdUserEmail", result.getEmail());
        redirectAttributes.addFlashAttribute("temporaryPassword", result.getTemporaryPassword());

        return "redirect:/webmaster/users";
    }

    // CREAR
    @PostMapping("/houses/create")
    public String createHouse(@RequestParam String name,
            @RequestParam boolean active) {

        houseService.createHouse(name, active);
        return "redirect:/webmaster/houses";
    }

    // EDITAR
    @PostMapping("/houses/edit/{id}")
    public String editHouse(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam boolean active) {

        houseService.updateHouse(id, name, active);
        return "redirect:/webmaster/houses";
    }

    // ELIMINAR
    @PostMapping("/houses/delete/{id}")
    public String deleteHouse(@PathVariable Long id) {
        houseService.deleteHouse(id);
        return "redirect:/webmaster/houses";
    }

    @GetMapping("/houses/{id}/admins")
    public String manageAdmins(@PathVariable Long id, Model model) {
        var house = houseService.getHouseById(id);
        if (house == null) {
            return "redirect:/webmaster/dashboard";
        }
        var admins = userService.getAdminsByHouse(id);
        model.addAttribute("house", house);
        model.addAttribute("admins", admins);
        return "webmaster/admins-house";
    }

    @PostMapping("/houses/{id}/admins/create")
    public String createAdmin(@PathVariable Long id,
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        try {
            userService.createAdmin(id, username, password);
            return "redirect:/webmaster/houses/" + id + "/admins?success";

        } catch (RuntimeException e) {

            var house = houseService.getHouseById(id);
            var admins = userService.getAdminsByHouse(id);

            model.addAttribute("house", house);
            model.addAttribute("admins", admins);
            model.addAttribute("errorMessage", e.getMessage());

            return "webmaster/manage-admins";
        }
    }

    // EDITAR ADMIN
    @PostMapping("/admins/edit/{id}")
    public String editAdmin(@PathVariable Long id,
            @RequestParam String username,
            @RequestParam boolean active) {

        adminService.updateAdmin(id, username, active);

        // Redirige a la gestión de admins de la misma house
        var houseId = adminService.getHouseIdByAdmin(id);
        return "redirect:/webmaster/houses/" + houseId + "/admins";
    }

    // ELIMINAR ADMIN
    @PostMapping("/admins/delete/{id}")
    public String deleteAdmin(@PathVariable Long id) {

        var houseId = adminService.getHouseIdByAdmin(id);
        adminService.deleteAdmin(id);

        return "redirect:/webmaster/houses/" + houseId + "/admins";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
            @RequestParam String username,
            @RequestParam boolean active,
            @RequestParam Long houseId,
            RedirectAttributes redirectAttributes) {

        try {
            userService.updateUser(id, username, active, houseId);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente");

        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/webmaster/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente");

        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/webmaster/users";
    }

    @GetMapping("/schedules")
    public String schedules(Model model) {

        var scheduleRuns = webmasterScheduleService.getAllRuns();

        model.addAttribute("scheduleRuns", scheduleRuns);

        return "webmaster/schedules";
    }

}
