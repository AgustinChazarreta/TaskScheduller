package com.AgsCh.task_scheduler.service.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.request.PersonUnavailabilityDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionAssignmentResponseDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionResponseDTO;
import com.AgsCh.task_scheduler.dto.response.PersonCreatedResponseDTO;
import com.AgsCh.task_scheduler.dto.response.PersonResponseDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.Group;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.PersonFunction;
import com.AgsCh.task_scheduler.model.PersonUnavailability;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.FunctionAssignmentRepository;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.GroupRepository;
import com.AgsCh.task_scheduler.repository.HouseRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.ScheduleRunRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;
import com.AgsCh.task_scheduler.service.admin.UserService;
import com.AgsCh.task_scheduler.service.storage.FileStorageService;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
public class PersonService {

    private final PersonRepository repository;
    private final FunctionRepository functionRepository;
    private final AdminScheduleService scheduleService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final FunctionAssignmentRepository functionAssignmentRepository;
    private final ScheduleRunRepository scheduleRunRepository;
    private final HouseRepository houseRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final GroupRepository groupRepository;

    public PersonService(
            PersonRepository repository,
            FunctionRepository functionRepository,
            AdminScheduleService scheduleService,
            FileStorageService fileStorageService,
            UserRepository userRepository,
            FunctionAssignmentRepository functionAssignmentRepository,
            ScheduleRunRepository scheduleRunRepository,
            HouseRepository houseRepository,
            PasswordEncoder passwordEncoder,
            UserService userService,
            GroupRepository groupRepository) {

        this.repository = repository;
        this.functionRepository = functionRepository;
        this.scheduleService = scheduleService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.functionAssignmentRepository = functionAssignmentRepository;
        this.scheduleRunRepository = scheduleRunRepository;
        this.houseRepository = houseRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.groupRepository = groupRepository;

    }

    // =====================================================
    // CREATE
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','WEBMASTER')")
    @Transactional
    public PersonCreatedResponseDTO create(PersonRequestDTO dto) {

        User currentUser = getCurrentUser();

        if (userRepository.existsByUsernameAndHouse(dto.getEmail(), currentUser.getHouse())) {
            throw new RuntimeException("Email already in use in this house");
        }

        Person person = new Person(
                dto.getFullName(),
                dto.getNickName(),
                dto.getBirthDate(),
                dto.getEmail(),
                dto.isEmailNotificationsEnabled(),
                dto.isActive(),
                dto.getEntryDate(),
                dto.getExitDate(),
                dto.getWorkingDays());

        person.setHouse(currentUser.getHouse());

        // GROUP
        if (dto.getGroupId() != null) {

            Group group = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found"));

            if (!group.getHouse().getId().equals(currentUser.getHouse().getId())) {
                throw new RuntimeException("Group does not belong to this house");
            }

            person.setGroup(group);
        }

        // FUNCIONES
        if (dto.getFunctionIds() != null && !dto.getFunctionIds().isEmpty()) {
            List<Function> functions = functionRepository.findAllById(dto.getFunctionIds());
            for (Function f : functions) {
                person.addPersonFunction(new PersonFunction(person, f));
            }
        }

        // 🔹 UNAVAILABILITIES
        if (dto.getUnavailabilities() != null) {
            dto.getUnavailabilities().forEach(uDto -> {
                if (uDto.getEndDate() != null) {
                    person.addUnavailability(
                            new PersonUnavailability(uDto.getStartDate(), uDto.getEndDate(), uDto.getReason()));
                } else {
                    person.addUnavailability(new PersonUnavailability(uDto.getStartDate(), uDto.getReason()));
                }
            });
        }

        Person savedPerson = repository.save(person);

        // PASSWORD TEMPORAL
        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setUsername(dto.getEmail());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setRole(Role.USER);
        user.setHouse(currentUser.getHouse());
        user.setPerson(savedPerson);

        userRepository.save(user);

        scheduleService.invalidate(savedPerson.getHouse());

        return new PersonCreatedResponseDTO(
                savedPerson.getId(),
                savedPerson.getFullName(),
                savedPerson.getEmail(),
                temporaryPassword);
    }

    @PreAuthorize("hasAnyRole('ADMIN','WEBMASTER')")
    @Transactional
    public PersonCreatedResponseDTO createForHouse(Long houseId, PersonRequestDTO dto) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            if (currentUser.getHouse() == null || !currentUser.getHouse().getId().equals(houseId)) {
                throw new RuntimeException("Access denied");
            }
        }

        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new RuntimeException("House not found"));

        if (userRepository.existsByUsernameAndHouse(dto.getEmail(), house)) {
            throw new RuntimeException("Email already in use in this house");
        }

        Person person = new Person(
                dto.getFullName(),
                dto.getNickName(),
                dto.getBirthDate(),
                dto.getEmail(),
                dto.isEmailNotificationsEnabled(),
                dto.isActive(),
                dto.getEntryDate(),
                dto.getExitDate(),
                dto.getWorkingDays());

        person.setHouse(house);

        // FUNCIONES
        if (dto.getFunctionIds() != null && !dto.getFunctionIds().isEmpty()) {
            List<Function> functions = functionRepository.findAllById(dto.getFunctionIds());
            for (Function f : functions) {
                if (!f.getHouse().getId().equals(houseId)) {
                    throw new RuntimeException("Function does not belong to this house");
                }
                person.addPersonFunction(new PersonFunction(person, f));
            }
        }

        // UNAVAILABILITIES
        if (dto.getUnavailabilities() != null && !dto.getUnavailabilities().isEmpty()) {
            for (PersonUnavailabilityDTO u : dto.getUnavailabilities()) {
                PersonUnavailability pu = new PersonUnavailability(u.getStartDate(), u.getEndDate(), u.getReason());
                person.addUnavailability(pu);
            }
        }

        Person savedPerson = repository.save(person);

        // PASSWORD TEMPORAL
        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setUsername(dto.getEmail());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setRole(Role.USER);
        user.setHouse(house);
        user.setPerson(savedPerson);

        userRepository.save(user);

        scheduleService.invalidate(house);

        return new PersonCreatedResponseDTO(
                savedPerson.getId(),
                savedPerson.getFullName(),
                savedPerson.getEmail(),
                temporaryPassword);
    }

    // =====================================================
    // READ
    // =====================================================

    public List<Person> findAll() {
        User currentUser = getCurrentUser();
        return repository.findByHouse(currentUser.getHouse());
    }

    public Person findById(Long id) {

        User currentUser = getCurrentUser();

        Person person = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        if (currentUser.getRole() != Role.WEBMASTER) {

            if (currentUser.getHouse() == null ||
                    !person.getHouse().getId().equals(currentUser.getHouse().getId())) {

                throw new RuntimeException("Access denied");
            }
        }

        return person;
    }

    public PersonResponseDTO getMyProfile() {
        return mapToResponseDTO(getCurrentUserPerson());
    }

    public PersonResponseDTO getMyProfileSafe() {
        return mapToResponseDTOSafe(getCurrentUserPerson());
    }

    private PersonResponseDTO mapToResponseDTO(Person person) {

        Set<FunctionResponseDTO> functions = person.getPersonFunctions()
                .stream()
                .map(pf -> new FunctionResponseDTO(
                        pf.getFunction().getId(),
                        pf.getFunction().getName(),
                        pf.getFunction().isSequential(),
                        pf.getFunction().getAssignedDays()))
                .collect(Collectors.toSet());

        return new PersonResponseDTO(
                person.getId(),
                person.getFullName(),
                person.getNickName(),
                person.getBirthDate(),
                person.getEmail(),
                person.isEmailNotificationsEnabled(),
                person.isActive(),
                person.getEntryDate(),
                person.getExitDate(),
                person.getWorkingDays(),
                functions,
                person.getProfileImageUrl());
    }

    // DTO seguro para perfil (no incluye assignedDays)
    public PersonResponseDTO mapToResponseDTOSafe(Person person) {

        // mapeamos funciones SIN assignedDays
        Set<FunctionResponseDTO> functions = person.getPersonFunctions()
                .stream()
                .map(pf -> new FunctionResponseDTO(
                        pf.getFunction().getId(),
                        pf.getFunction().getName(),
                        pf.getFunction().isSequential(),
                        null // 🔹 no traemos assignedDays
                ))
                .collect(Collectors.toSet());
        // UNAVAILABILITIES
        Set<PersonUnavailabilityDTO> unavailabilities = person.getUnavailabilities()
                .stream()
                .map(u -> new PersonUnavailabilityDTO(
                        u.getStartDate(),
                        u.getEndDate(),
                        u.getReason()))
                .collect(Collectors.toSet());

        return new PersonResponseDTO(
                person.getId(),
                person.getFullName(),
                person.getNickName(),
                person.getBirthDate(),
                person.getEmail(),
                person.isEmailNotificationsEnabled(),
                person.isActive(),
                person.getEntryDate(),
                person.getExitDate(),
                person.getWorkingDays(),
                functions,
                unavailabilities,
                person.getProfileImageUrl(),
                person.getUser() != null ? person.getUser().getRole().name() : null,
                person.getGroup() != null ? person.getGroup().getId() : null,
                person.getGroup() != null ? person.getGroup().getName() : null,
                person.getHouse() != null ? person.getHouse().getName() : null);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @Transactional
    public void update(Long id, PersonRequestDTO dto) {

        Person person = findById(id);

        person.setFullName(dto.getFullName());
        person.setNickName(dto.getNickName());
        person.setBirthDate(dto.getBirthDate());
        person.setEmail(dto.getEmail());
        person.setEmailNotificationsEnabled(dto.isEmailNotificationsEnabled());
        person.setActive(dto.isActive());
        person.setEntryDate(dto.getEntryDate());
        person.setExitDate(dto.getExitDate());
        person.setWorkingDays(dto.getWorkingDays());

        // GROUP
        if (dto.getGroupId() != null) {

            Group group = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found"));

            person.setGroup(group);

        } else {

            person.setGroup(null);

        }

        // FUNCIONES
        Set<Long> newFunctionIds = dto.getFunctionIds() != null ? dto.getFunctionIds() : Set.of();
        person.getPersonFunctions()
                .removeIf(pf -> !newFunctionIds.contains(pf.getFunction().getId()));

        for (Long functionId : newFunctionIds) {
            boolean exists = person.getPersonFunctions().stream()
                    .anyMatch(pf -> pf.getFunction().getId().equals(functionId));
            if (!exists) {
                Function f = functionRepository.findById(functionId)
                        .orElseThrow(() -> new RuntimeException("Function not found"));
                person.addPersonFunction(new PersonFunction(person, f));
            }
        }

        // 🔹 UNAVAILABILITIES
        person.getUnavailabilities().clear(); // borramos las viejas
        if (dto.getUnavailabilities() != null) {
            dto.getUnavailabilities().forEach(uDto -> {
                if (uDto.getEndDate() != null) {
                    person.addUnavailability(
                            new PersonUnavailability(uDto.getStartDate(), uDto.getEndDate(), uDto.getReason()));
                } else {
                    person.addUnavailability(new PersonUnavailability(uDto.getStartDate(), uDto.getReason()));
                }
            });
        }

        scheduleService.invalidate(person.getHouse());
    }

    @Transactional
    public void updateMyProfile(PersonRequestDTO dto) {

        Person person = getCurrentUserPerson();

        person.setFullName(dto.getFullName());
        person.setNickName(dto.getNickName());
        person.setBirthDate(dto.getBirthDate());
        person.setEmail(dto.getEmail());
        person.setEmailNotificationsEnabled(dto.isEmailNotificationsEnabled());
        person.setEntryDate(dto.getEntryDate());
        person.setExitDate(dto.getExitDate());

        // ❌ NO permitimos:
        // - cambiar funciones
        // - cambiar active
        // - cambiar workingDays
    }

    // =====================================================
    // DELETE
    // =====================================================

    @Transactional
    public void delete(Long id) {

        Person person = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Persona no encontrada"));

        scheduleService.invalidate(person.getHouse());

        if (person.getUser() != null) {
            userService.deleteUser(person.getUser().getId());
        } else {
            repository.delete(person);
        }
    }

    // =====================================================
    // PROFILE IMAGE
    // =====================================================

    @Transactional
    public String uploadProfileImage(Long personId, MultipartFile file) {

        Person person = findById(personId);

        try {

            if (person.getProfileImagePublicId() != null) {
                fileStorageService.deleteFile(person.getProfileImagePublicId());
            }

            String newUrl = fileStorageService.uploadFile(file);
            String newPublicId = fileStorageService.getLastUploadedPublicId();

            person.setProfileImageUrl(newUrl);
            person.setProfileImagePublicId(newPublicId);

            repository.save(person);

            return newUrl;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading profile image", e);
        }
    }

    @Transactional
    public String uploadMyProfileImage(MultipartFile file) {
        return uploadProfileImage(getCurrentUserPerson().getId(), file);
    }

    // =====================================================
    // SCHEDULE
    // =====================================================

    public ScheduleResponseDTO getMySchedule() {

        Person person = getCurrentUserPerson();

        ScheduleRun activeRun = scheduleRunRepository
                .findByHouseIdAndStatus(
                        person.getHouse().getId(),
                        ScheduleRun.Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active schedule"));

        List<FunctionAssignmentResponseDTO> myAssignments = functionAssignmentRepository
                .findByScheduleRun_IdAndPerson_Id(
                        activeRun.getId(),
                        person.getId())
                .stream()
                .map(FunctionAssignmentResponseDTO::fromEntity)
                .toList();

        return new ScheduleResponseDTO(
                myAssignments,
                activeRun.getScore());
    }

    // =====================================================
    // PRIVATE HELPERS
    // =====================================================

    private User getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Person getCurrentUserPerson() {

        User user = getCurrentUser();

        if (user.getPerson() == null) {
            throw new RuntimeException("Current user is not linked to a person");
        }

        return user.getPerson();
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
    }
}