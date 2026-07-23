package com.AgsCh.task_scheduler.service.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AgsCh.task_scheduler.dto.request.GroupRequestDTO;
import com.AgsCh.task_scheduler.dto.response.GroupResponseDTO;
import com.AgsCh.task_scheduler.dto.response.PersonResponseDTO;
import com.AgsCh.task_scheduler.model.Group;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.GroupRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.service.domain.PersonService;
import com.AgsCh.task_scheduler.repository.FunctionRepository;

@Service
public class GroupService {

        private final GroupRepository groupRepository;
        private final UserRepository userRepository;
        private final PersonRepository personRepository;
        private final FunctionRepository functionRepository;
        private final PersonService personService;

        public GroupService(GroupRepository groupRepository,
                        UserRepository userRepository,
                        PersonRepository personRepository,
                        FunctionRepository functionRepository,
                        PersonService personService) {
                this.groupRepository = groupRepository;
                this.userRepository = userRepository;
                this.personRepository = personRepository;
                this.functionRepository = functionRepository;
                this.personService = personService;
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
                                                                .collect(Collectors.toList()),
                                                g.getWorkingDays(),
                                                g.getFunctions()
                                                                .stream()
                                                                .map(Function::getId)
                                                                .collect(Collectors.toSet())))
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<GroupResponseDTO> findByHouseId(Long houseId) {

                return groupRepository.findByHouseId(houseId)
                                .stream()
                                .map(g -> new GroupResponseDTO(
                                                g.getId(),
                                                g.getName(),
                                                g.getPersons()
                                                                .stream()
                                                                .map(this::mapPerson)
                                                                .collect(Collectors.toList()),
                                                g.getWorkingDays(),
                                                g.getFunctions()
                                                                .stream()
                                                                .map(Function::getId)
                                                                .collect(Collectors.toSet())))
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

                group.setWorkingDays(dto.getWorkingDays());

                if (dto.getFunctionIds() != null) {

                        Set<Function> functions = new HashSet<>(
                                        functionRepository.findByHouseIdAndIdIn(
                                                        user.getHouse().getId(),
                                                        dto.getFunctionIds()));

                        group.setFunctions(functions);
                }

                if (dto.getPersonIds() != null && !dto.getPersonIds().isEmpty()) {
                        List<Person> persons = personRepository.findAllById(dto.getPersonIds());

                        for (Person person : persons) {

                                if (!person.getHouse().getId().equals(user.getHouse().getId())) {
                                        throw new RuntimeException("Person does not belong to this house");
                                }
                        }

                        group.addPersons(persons);
                }

                Group saved = groupRepository.save(group);

                synchronizeGroup(saved);

                return new GroupResponseDTO(
                                saved.getId(),
                                saved.getName(),
                                saved.getPersons()
                                                .stream()
                                                .map(this::mapPerson)
                                                .collect(Collectors.toList()),
                                saved.getWorkingDays(),
                                saved.getFunctions()
                                                .stream()
                                                .map(Function::getId)
                                                .collect(Collectors.toSet()));
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

                group.setWorkingDays(dto.getWorkingDays());

                if (dto.getFunctionIds() != null) {

                        Set<Function> functions = new HashSet<>(
                                        functionRepository.findByHouseIdAndIdIn(
                                                        user.getHouse().getId(),
                                                        dto.getFunctionIds()));

                        group.setFunctions(functions);
                }

                if (dto.getPersonIds() != null) {

                        List<Person> previousPersons = new ArrayList<>(group.getPersons());

                        group.getPersons().forEach(p -> p.setGroup(null));
                        group.getPersons().clear();

                        if (!dto.getPersonIds().isEmpty()) {
                                List<Person> persons = personRepository.findAllById(dto.getPersonIds());

                                for (Person person : persons) {

                                        if (!person.getHouse().getId().equals(user.getHouse().getId())) {
                                                throw new RuntimeException("Person does not belong to this house");
                                        }
                                }

                                group.addPersons(persons);
                        }

                        for (Person person : previousPersons) {

                                if (person.getGroup() == null) {

                                        person.getAdditionalWorkingDays().clear();
                                        person.getRemovedWorkingDays().clear();

                                        person.getAdditionalFunctions().clear();
                                        person.getRemovedFunctions().clear();

                                        personService.rebuildConfiguration(person);
                                }
                        }
                }

                Group saved = groupRepository.save(group);

                synchronizeGroup(saved);

                return new GroupResponseDTO(
                                saved.getId(),
                                saved.getName(),
                                saved.getPersons()
                                                .stream()
                                                .map(this::mapPerson)
                                                .collect(Collectors.toList()),
                                saved.getWorkingDays(),
                                saved.getFunctions()
                                                .stream()
                                                .map(Function::getId)
                                                .collect(Collectors.toSet()));
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
                List<Person> persons = new ArrayList<>(group.getPersons());

                for (Person person : persons) {
                        person.setGroup(null);
                        person.getAdditionalWorkingDays().clear();
                        person.getRemovedWorkingDays().clear();
                        person.getAdditionalFunctions().clear();
                        person.getRemovedFunctions().clear();
                        personService.rebuildConfiguration(person);
                }

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
                                person.getFullName(),
                                person.getNickName());
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

        private void synchronizeGroup(Group group) {
                for (Person person : group.getPersons()) {
                        synchronizePerson(person);
                }
        }

        private void synchronizePerson(Person person) {
                personService.rebuildConfiguration(person);
        }
}