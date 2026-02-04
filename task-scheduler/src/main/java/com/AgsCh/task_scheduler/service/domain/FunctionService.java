package com.AgsCh.task_scheduler.service.domain;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.FunctionRequestDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;
import com.AgsCh.task_scheduler.util.normalizer.TaskRefactor;
import com.AgsCh.task_scheduler.util.word.WordParser;

@Service
public class FunctionService {

    private final FunctionRepository repository;
    private final AdminScheduleService scheduleService;

    public FunctionService(FunctionRepository repository, AdminScheduleService scheduleService) {
        this.repository = repository;
        this.scheduleService = scheduleService;
    }

    // -------- CREATE --------
    public Function create(FunctionRequestDTO dto) {
        Function function = new Function(
                dto.getName(),
                dto.isSequential(),
                dto.getAssignedDays());
        scheduleService.invalidate();
        return repository.save(function);
    }
    
    // -------- READ --------
    public List<Function> findAll() {
        return repository.findAll();
    }
    
    public Function findById(Long id) {
        return repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Function not found"));
    }
    
    // -------- UPDATE --------
    public void update(Long id, FunctionRequestDTO dto) {
        Function function = findById(id);
        
        function.setName(dto.getName());
        function.setSequential(dto.isSequential());
        function.setAssignedDays(dto.getAssignedDays());
        
        scheduleService.invalidate();
        repository.save(function);
    }
    
    // -------- DELETE --------
    public void delete(Long id) {
        repository.deleteById(id);
        scheduleService.invalidate();
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
    public void createFunctions(List<FunctionRequestDTO> tasks) {
        tasks.forEach(this::create);
    }
}
