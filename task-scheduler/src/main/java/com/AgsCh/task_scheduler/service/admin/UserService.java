package com.AgsCh.task_scheduler.service.admin;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.User;
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

    public UserService(
            UserRepository userRepository,
            HouseRepository houseRepository,
            PasswordEncoder passwordEncoder,
            PersonRepository personRepository) {

        this.userRepository = userRepository;
        this.houseRepository = houseRepository;
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
    }

    public User createAdmin(Long houseId, String username, String rawPassword) {

        // Validar username duplicado
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Ya existe un admin con ese username");
        }

        // Buscar house
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new BusinessException("House no encontrada"));

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(Role.ADMIN);
        admin.setHouse(house);
        admin.setActive(true);

        return userRepository.save(admin);
    }

    public List<User> getAdminsByHouse(Long houseId) {
        return userRepository.findByHouseIdAndRole(houseId, Role.ADMIN);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != Role.WEBMASTER)
                .toList();
    }

    public List<User> getAllUsersPerson() {
        return userRepository.findByRole(Role.USER);
    }

    public List<User> getAllAdmins() {
        return userRepository.findByRole(Role.ADMIN);
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("No hay usuario autenticado");
        }
        String username = auth.getName(); // el username logueado
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    public void updateUser(Long id, String username, boolean active, Long houseId) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Evitar modificar WEBMASTER
        if (user.getRole() == Role.WEBMASTER) {
            throw new BusinessException("No se puede modificar el usuario WEBMASTER");
        }

        // Validar username duplicado (si cambió)
        if (!user.getUsername().equals(username)
                && userRepository.existsByUsername(username)) {
            throw new BusinessException("Ya existe un usuario con ese username");
        }

        // Buscar nueva house
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new BusinessException("House no encontrada"));

        user.setUsername(username);
        user.setActive(active);
        user.setHouse(house);

        userRepository.save(user);
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
            // romper relación bidireccional
            user.setPerson(null);
            person.setUser(null);

            userRepository.save(user); // actualizar FK a null
            personRepository.delete(person);
        }

        userRepository.delete(user);
    }
}