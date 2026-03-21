package com.AgsCh.task_scheduler.service.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.response.PersonCreatedResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.Group;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.PersonFunction;
import com.AgsCh.task_scheduler.model.PersonUnavailability;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.GroupRepository;
import com.AgsCh.task_scheduler.repository.HouseRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final HouseRepository houseRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;
    private final GroupRepository groupRepository;
    private final FunctionRepository functionRepository;

    public UserService(
            UserRepository userRepository,
            HouseRepository houseRepository,
            PasswordEncoder passwordEncoder,
            PersonRepository personRepository,
            GroupRepository groupRepository,
            FunctionRepository functionRepository) {

        this.userRepository = userRepository;
        this.houseRepository = houseRepository;
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
        this.groupRepository = groupRepository;
        this.functionRepository = functionRepository;
    }

    @Transactional
    public User createAdmin(Long houseId, String username, String temporaryPassword) {

        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new BusinessException("House no encontrada"));

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Ya existe un usuario con ese email");
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(temporaryPassword));
        admin.setPasswordTemporary(true);
        admin.setRole(Role.ADMIN);
        admin.setHouse(house);
        admin.setActive(true);

        return userRepository.save(admin);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != Role.WEBMASTER)
                .toList();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public List<User> getAllUsersPerson() {
        return userRepository.findByRole(Role.USER);
    }

    // Trae todos los admins con AdminData incluido
    public List<User> getAllAdmins() {
        return userRepository.findAllAdminsWithAdminData();
    }

    // Trae los admins de una casa específica con AdminData incluido
    public List<User> getAdminsByHouse(Long houseId) {
        return userRepository.findAdminsByHouseIdWithAdminData(houseId);
    }

    // Trae los admins pendientes (inactivos) con AdminData incluido
    public List<User> getPendingAdmins() {
        return userRepository.findAllAdminsWithAdminData()
                .stream()
                .filter(u -> !u.isActive())
                .toList();
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("No hay usuario autenticado");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    public PersonCreatedResponseDTO updateUser(Long id, PersonRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != Role.USER)
            throw new RuntimeException("Solo se pueden actualizar usuarios normales");

        Person person = user.getPerson();
        if (person == null)
            throw new RuntimeException("El usuario no tiene persona asociada");

        // DATOS BÁSICOS
        person.setFullName(dto.getFullName());
        person.setNickName(dto.getNickName());
        person.setBirthDate(dto.getBirthDate());
        person.setEmail(dto.getEmail());
        person.setEmailNotificationsEnabled(dto.isEmailNotificationsEnabled());
        person.setActive(dto.isActive());

        // FECHAS
        person.setEntryDate(dto.getEntryDate());
        person.setExitDate(dto.getExitDate());

        // CASA
        if (dto.getHouseId() != null) {
            House house = houseRepository.findById(dto.getHouseId())
                    .orElseThrow(() -> new RuntimeException("House no encontrada"));
            person.setHouse(house);
            user.setHouse(house);
        }

        // GRUPO
        if (dto.getGroupId() != null) {
            Group group = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
            person.setGroup(group);
            person.setHouse(group.getHouse());
            user.setHouse(group.getHouse());
        }

        // FUNCIONES
        List<PersonFunction> currentFunctions = new ArrayList<>(person.getPersonFunctions());
        Set<Long> newFunctionIds = new HashSet<>(dto.getFunctionIds());
        currentFunctions.stream()
                .filter(pf -> !newFunctionIds.contains(pf.getFunction().getId()))
                .forEach(person::removePersonFunction);
        for (Long fid : newFunctionIds) {
            boolean exists = person.getPersonFunctions().stream()
                    .anyMatch(pf -> pf.getFunction().getId().equals(fid));
            if (!exists) {
                Function f = functionRepository.findById(fid)
                        .orElseThrow(() -> new RuntimeException("Función no encontrada: " + fid));
                PersonFunction pf = new PersonFunction();
                pf.setPerson(person);
                pf.setFunction(f);
                person.addPersonFunction(pf);
            }
        }

        // DÍAS DE TRABAJO
        person.setWorkingDays(dto.getWorkingDays());

        // AUSENCIAS
        person.getUnavailabilities().clear();
        if (dto.getUnavailabilities() != null) {
            dto.getUnavailabilities().stream()
                    .map(u -> new PersonUnavailability(u.getStartDate(), u.getEndDate(), u.getReason()))
                    .forEach(person::addUnavailability);
        }

        personRepository.save(person);
        userRepository.save(user);

        // 🚀 RETORNAR personId para poder subir la foto
        return new PersonCreatedResponseDTO(person);
    }

    @Transactional
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (user.getRole() == Role.WEBMASTER) {
            throw new BusinessException("No se puede eliminar el WEBMASTER");
        }

        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new BusinessException("No se puede eliminar el último administrador");
            }
        }

        // 🔥 Si tiene persona asociada, eliminar correctamente
        Person person = user.getPerson();

        if (person != null) {
            user.setPerson(null);
            personRepository.delete(person);
        }

        userRepository.delete(user);
    }

    @Transactional
    public void changeMyPassword(String newPassword) {

        User user = getAuthenticatedUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordTemporary(false);

        userRepository.save(user);
    }

    @Transactional
    public void changeMyEmail(String email) {

        if (userRepository.existsByUsername(email)) {
            throw new BusinessException("El email ya está en uso");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String currentEmail = auth.getName();

        User user = userRepository.findByUsername(currentEmail)
                .orElseThrow();

        user.setUsername(email);

        userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByUsernameIgnoreCase(email);
    }

    public List<User> getAllUsersWithAdminData() {
        // Trae todos los usuarios, carga AdminData para admins y filtra WEBMASTER
        return userRepository.findAllWithAdminData()
                .stream()
                .filter(u -> u.getRole() != Role.WEBMASTER)
                .toList();
    }
}