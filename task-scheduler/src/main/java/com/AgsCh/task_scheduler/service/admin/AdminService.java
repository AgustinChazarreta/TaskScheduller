package com.AgsCh.task_scheduler.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.model.FunctionRule;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.FunctionRuleRepository;

@Service
public class AdminService {

    private final AdminScheduleService adminScheduleService;
    private final FunctionRepository functionRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final FunctionRuleRepository functionRuleRepository;

    public AdminService(
            AdminScheduleService adminScheduleService,
            FunctionRepository functionRepository,
            UserRepository userRepository,
            PersonRepository personRepository,
            FunctionRuleRepository functionRuleRepository) {

        this.adminScheduleService = adminScheduleService;
        this.functionRepository = functionRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.functionRuleRepository = functionRuleRepository;
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
    public User updateAdmin(Long id, String username, boolean active) {
        User admin = getAdminById(id); // buscar admin por id
        admin.setUsername(username); // actualizar username
        admin.setActive(active); // actualizar estado
        return userRepository.save(admin); // guardar cambios
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
