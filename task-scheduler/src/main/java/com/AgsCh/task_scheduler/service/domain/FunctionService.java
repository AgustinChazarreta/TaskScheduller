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
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.util.normalizer.TaskRefactor;
import com.AgsCh.task_scheduler.util.word.WordParser;

@Service
public class FunctionService {

    private final FunctionRepository repository;
    private final UserRepository userRepository;

    public FunctionService(FunctionRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
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
        return repository.findByHouseId(currentUser.getHouse().getId());
    }

    public Function findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));
    }

    public List<Function> findByHouseId(Long houseId) {
        return repository.findByHouseId(houseId);
    }

    // -------- UPDATE --------
    public void update(Long id, FunctionRequestDTO dto) {
        Function function = findById(id);

        function.setName(dto.getName());
        function.setSequential(dto.isSequential());
        function.setAssignedDays(dto.getAssignedDays());

        repository.save(function);
    }

    // -------- DELETE --------
    public void delete(Long id) {
        repository.deleteById(id);
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
