package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.AgsCh.task_scheduler.dto.request.PersonUnavailabilityDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.User;

public class UserResponseDTO {

    // ================= USER =================

    public Long id;
    public String username;
    public String role;
    public boolean active;

    public LocalDateTime createdAt;

    // ================= HOUSE =================

    public Long houseId;
    public String houseName;

    // ================= PERSON =================

    public Long personId;

    public String fullName;
    public String nickName;
    public LocalDate birthDate;
    public String email;

    public boolean emailNotificationsEnabled;

    public String profileImageUrl;

    public LocalDate entryDate;
    public LocalDate exitDate;

    // ================= GROUP =================

    public Long groupId;
    public String groupName;

    // ================= FUNCTIONS =================

    public List<Long> functionIds;
    public List<String> functionNames;

    // ================= WORKING DAYS =================

    public Set<DayOfWeek> workingDays;

    // ================= UNAVAILABILITIES =================

    public List<PersonUnavailabilityDTO> unavailabilities;

    // ================= PASSWORD TEMP =================

    public String temporaryPassword;

    // ================= ADMIN-DATA =================
    public AdminDataDTO adminData;

    // ================= CONSTRUCTORS =================

    public UserResponseDTO(User user) {
        this(user, null);
    }

    public UserResponseDTO(User user, String temporaryPassword) {

        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().name();
        if (user.getRole() == Role.USER && user.getPerson() != null) {
            this.active = user.getPerson().isActive(); // ahora sí es el active de Person
        } else {
            this.active = user.isActive(); // para admins
        }
        this.createdAt = user.getCreatedAt();
        this.temporaryPassword = temporaryPassword;

        if (user.getHouse() != null) {
            this.houseId = user.getHouse().getId();
            this.houseName = user.getHouse().getName();
        }

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.WEBMASTER) {
            if (user.getAdminData() != null) {
                this.adminData = new AdminDataDTO(user.getAdminData());
            }
        }

        Person p = user.getPerson();

        if (p != null) {

            this.personId = p.getId();
            this.fullName = p.getFullName();
            this.nickName = p.getNickName();
            this.birthDate = p.getBirthDate();
            this.email = p.getEmail();

            this.emailNotificationsEnabled = p.isEmailNotificationsEnabled();

            this.profileImageUrl = p.getProfileImageUrl();

            this.entryDate = p.getEntryDate();
            this.exitDate = p.getExitDate();

            if (p.getGroup() != null) {
                this.groupId = p.getGroup().getId();
                this.groupName = p.getGroup().getName();
            }

            if (p.getPersonFunctions() != null && !p.getPersonFunctions().isEmpty()) {
                this.functionIds = p.getPersonFunctions()
                        .stream()
                        .map(pf -> pf.getFunction().getId())
                        .collect(Collectors.toList());

                this.functionNames = p.getPersonFunctions()
                        .stream()
                        .map(pf -> pf.getFunction().getName())
                        .collect(Collectors.toList());
            }

            this.workingDays = p.getWorkingDays();

            if (p.getUnavailabilities() != null) {

                this.unavailabilities = p.getUnavailabilities()
                        .stream()
                        .map(PersonUnavailabilityDTO::new)
                        .collect(Collectors.toList());
            }
        }
    }
}