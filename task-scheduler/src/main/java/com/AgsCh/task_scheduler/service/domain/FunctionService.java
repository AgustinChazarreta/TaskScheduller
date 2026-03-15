package com.AgsCh.task_scheduler.service.domain;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.FunctionRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;
import com.AgsCh.task_scheduler.util.normalizer.TaskRefactor;
import com.AgsCh.task_scheduler.util.word.WordParser;

import jakarta.transaction.Transactional;

@Service
public class FunctionService {

    private final FunctionRepository repository;
    private final UserRepository userRepository;
    private final AdminScheduleService scheduleService;

    public FunctionService(FunctionRepository repository, UserRepository userRepository,
            AdminScheduleService scheduleService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.scheduleService = scheduleService;
    }

    // -------- CREATE --------
    public Function create(FunctionRequestDTO dto, House house) {

        Function function = new Function(
                dto.getName(),
                dto.isSequential(),
                dto.getAssignedDays());

        function.setHouse(house); // 🔥 LA LÍNEA CLAVE

        return repository.save(function);
    }

    // -------- READ --------
    public List<Function> findAll() {
        User currentUser = getCurrentUser();
        return repository.findByHouseIdAndActiveTrue(currentUser.getHouse().getId());
    }

    public Function findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));
    }

    public List<FunctionResponseDTO> findDTOByHouseId(Long houseId) {
        return repository.findByHouseIdAndActiveTrue(houseId)
                .stream()
                .map(this::map)
                .toList();
    }

    private FunctionResponseDTO map(Function function) {
        return new FunctionResponseDTO(
                function.getId(),
                function.getName(),
                function.isSequential(),
                function.getAssignedDays());
    }

    // -------- UPDATE --------
    public void update(Long id, FunctionRequestDTO dto) {
        Function function = findById(id);

        function.setName(dto.getName());
        function.setSequential(dto.isSequential());
        function.setAssignedDays(dto.getAssignedDays());

        repository.save(function);
        scheduleService.invalidate(function.getHouse());
    }

    // -------- DELETE --------
    @Transactional
    public void delete(Long id) {

        Function function = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Función no encontrada"));

        function.setActive(false);
        repository.save(function);
        scheduleService.invalidate(function.getHouse());
    }

    // -------- LOAD WORD --------
    public Map<String, Set<DayOfWeek>> parseTasksFromWord(MultipartFile file) {

        if (file.isEmpty()) {
            throw new BusinessException("Archivo vacío");
        }

        Map<String, List<String>> raw = WordParser.parseTasks(file);
        return TaskRefactor.refactorDays(raw);
    }

    // -------- CREATE FUNCTIONS--------
    public void createFunctions(List<FunctionRequestDTO> tasks, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        House house = user.getHouse();
        tasks.forEach(dto -> create(dto, house));
    }

    // -------- HELPERS --------
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return getUser(auth.getName());
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
