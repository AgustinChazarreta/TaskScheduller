package com.AgsCh.task_scheduler.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.model.FunctionRule;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.FunctionRuleRepository;
import com.AgsCh.task_scheduler.repository.HouseRepository;

@Service
public class AdminService {

    private final AdminScheduleService adminScheduleService;
    private final FunctionRepository functionRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final FunctionRuleRepository functionRuleRepository;
    private final HouseRepository houseRepository;

    public AdminService(
            AdminScheduleService adminScheduleService,
            FunctionRepository functionRepository,
            UserRepository userRepository,
            PersonRepository personRepository,
            FunctionRuleRepository functionRuleRepository,
            HouseRepository houseRepository) {

        this.adminScheduleService = adminScheduleService;
        this.functionRepository = functionRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.functionRuleRepository = functionRuleRepository;
        this.houseRepository = houseRepository;
    }

    /*
     * =========================
     * GENERAR Y RESOLVER
     * =========================
     */
    // DTO → dominio
    public Schedule generateAndSolve(ScheduleRequestDTO request, User user) {

        // 1️⃣ Construcción base (sin rules)
        Schedule schedule = ScheduleMapper.toModel(
                request,
                functionRepository,
                personRepository);

        // 2️⃣ Cargar reglas de la house
        List<FunctionRule> rules = functionRuleRepository.findByHouseId(user.getHouse().getId());

        // 3️⃣ Inyectarlas en el mismo Schedule
        schedule.setFunctionRuleList(rules);

        // 4️⃣ Resolver y persistir
        return adminScheduleService.solve(schedule, user.getHouse());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // actualizar admin
    public void updateAdmin(Long id, String username, boolean active, Long houseId) {
        User admin = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        admin.setUsername(username);
        admin.setActive(active);

        if (houseId != null) {
            House house = houseRepository.findById(houseId)
                    .orElseThrow(() -> new RuntimeException("House no encontrada"));
            admin.setHouse(house);
        }

        userRepository.save(admin);
    }

    // eliminar admin
    public void deleteAdmin(Long id) {
        User admin = getAdminById(id); // verificar que exista
        userRepository.delete(admin);
    }

    // obtener admin por id
    public User getAdminById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("User is not an admin");
        }
        return user;
    }

    // opcional: obtener houseId del admin (para redirección)
    public Long getHouseIdByAdmin(Long id) {
        User admin = getAdminById(id);
        return admin.getHouse().getId();
    }
}
