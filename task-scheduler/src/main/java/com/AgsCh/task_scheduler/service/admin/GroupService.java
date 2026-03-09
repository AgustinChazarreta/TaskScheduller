package com.AgsCh.task_scheduler.service.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AgsCh.task_scheduler.dto.request.GroupRequestDTO;
import com.AgsCh.task_scheduler.dto.response.GroupResponseDTO;
import com.AgsCh.task_scheduler.dto.response.PersonResponseDTO;
import com.AgsCh.task_scheduler.model.Group;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.GroupRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;

    public GroupService(GroupRepository groupRepository,
            UserRepository userRepository,
            PersonRepository personRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
    }

    /*
     * =========================================
     * LISTAR GRUPOS
     * =========================================
     */

    @Transactional(readOnly = true)
    public List<GroupResponseDTO> findAll() {

        User user = getCurrentUser();

        return groupRepository.findByHouse(user.getHouse())
                .stream()
                .map(g -> new GroupResponseDTO(
                        g.getId(),
                        g.getName(),
                        g.getPersons()
                                .stream()
                                .map(this::mapPerson)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    /*
     * =========================================
     * CREAR GRUPO
     * =========================================
     */

    @Transactional
    public GroupResponseDTO create(GroupRequestDTO dto) {

        User user = getCurrentUser();

        Group group = new Group(dto.getName(), user.getHouse());

        if (dto.getPersonIds() != null && !dto.getPersonIds().isEmpty()) {

            List<Person> persons = personRepository.findAllById(dto.getPersonIds());

            group.addPersons(persons);
        }

        Group saved = groupRepository.save(group);

        return new GroupResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getPersons()
                        .stream()
                        .map(this::mapPerson)
                        .collect(Collectors.toList()));
    }

    /*
     * =========================================
     * ACTUALIZAR GRUPO
     * =========================================
     */

    @Transactional
    public GroupResponseDTO update(Long id, GroupRequestDTO dto) {

        User user = getCurrentUser();

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        if (!group.getHouse().equals(user.getHouse())) {
            throw new RuntimeException("No tiene permisos para editar este grupo");
        }

        group.setName(dto.getName());

        if (dto.getPersonIds() != null) {

            // quitar relación actual
            group.getPersons().forEach(p -> p.setGroup(null));
            group.getPersons().clear();

            // agregar nuevas personas
            if (!dto.getPersonIds().isEmpty()) {

                List<Person> persons = personRepository.findAllById(dto.getPersonIds());

                group.addPersons(persons);
            }
        }

        Group saved = groupRepository.save(group);

        return new GroupResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getPersons()
                        .stream()
                        .map(this::mapPerson)
                        .collect(Collectors.toList()));
    }

    /*
     * =========================================
     * ELIMINAR GRUPO
     * =========================================
     */

    @Transactional
    public void delete(Long id) {

        User user = getCurrentUser();

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        if (!group.getHouse().equals(user.getHouse())) {
            throw new RuntimeException("No tiene permisos para eliminar este grupo");
        }

        // quitar relación con personas
        group.getPersons().forEach(person -> person.setGroup(null));
        group.getPersons().clear();

        groupRepository.delete(group);
    }

    /*
     * =========================================
     * MAPPER PERSON
     * =========================================
     */

    private PersonResponseDTO mapPerson(Person person) {
        return new PersonResponseDTO(
                person.getId(),
                person.getFullName());
    }

    /*
     * =========================================
     * OBTENER USUARIO ACTUAL
     * =========================================
     */

    private User getCurrentUser() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}