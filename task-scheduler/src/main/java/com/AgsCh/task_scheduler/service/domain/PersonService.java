package com.AgsCh.task_scheduler.service.domain;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.PersonFunction;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;
import com.AgsCh.task_scheduler.service.storage.FileStorageService;

import jakarta.transaction.Transactional;

@Service
public class PersonService {

    private final PersonRepository repository;
    private final FunctionRepository functionRepository;
    private final AdminScheduleService scheduleService;
    private final FileStorageService fileStorageService;

    public PersonService(
            PersonRepository repository,
            FunctionRepository functionRepository,
            AdminScheduleService scheduleService,
            FileStorageService fileStorageService) {

        this.repository = repository;
        this.functionRepository = functionRepository;
        this.scheduleService = scheduleService;
        this.fileStorageService = fileStorageService;
    }

    // -------- CREATE --------
    @Transactional
    public Person create(PersonRequestDTO dto) {

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

        if (dto.getFunctionIds() != null && !dto.getFunctionIds().isEmpty()) {
            List<Function> functions = functionRepository.findAllById(dto.getFunctionIds());

            for (Function f : functions) {
                person.addPersonFunction(new PersonFunction(person, f));
            }
        }

        scheduleService.invalidate();
        return repository.save(person);
    }

    // -------- READ --------
    public List<Person> findAll() {
        return repository.findAll();
    }

    public Person findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
    }

    // -------- UPDATE --------
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

        // ---- FUNCIONES (sync real con DELETE) ----
        Set<Long> newFunctionIds = dto.getFunctionIds() != null ? dto.getFunctionIds() : Set.of();

        // eliminar relaciones que ya no estén
        person.getPersonFunctions().removeIf(pf -> !newFunctionIds.contains(pf.getFunction().getId()));

        // agregar nuevas relaciones
        for (Long functionId : newFunctionIds) {

            boolean exists = person.getPersonFunctions().stream()
                    .anyMatch(pf -> pf.getFunction().getId().equals(functionId));

            if (!exists) {
                Function f = functionRepository.findById(functionId)
                        .orElseThrow(() -> new RuntimeException("Function not found"));

                person.addPersonFunction(new PersonFunction(person, f));
            }
        }

        scheduleService.invalidate();
    }

    // -------- DELETE --------
    public void delete(Long id) {
        repository.deleteById(id);
        scheduleService.invalidate();
    }

    // -------- UPLOAD / REPLACE PROFILE IMAGE --------
    @Transactional
    public String uploadProfileImage(Long personId, MultipartFile file) {

        Person person = repository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        try {
            // 1️⃣ borrar imagen anterior si existe
            if (person.getProfileImagePublicId() != null) {
                fileStorageService.deleteFile(person.getProfileImagePublicId());
            }

            // 2️⃣ subir nueva imagen usando FileStorageService
            String newUrl = fileStorageService.uploadFile(file);
            String newPublicId = fileStorageService.getLastUploadedPublicId(); // si tu servicio guarda el último
                                                                               // publicId

            // 3️⃣ guardar info en la entidad
            person.setProfileImageUrl(newUrl);
            person.setProfileImagePublicId(newPublicId);

            repository.save(person);

            return newUrl;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading profile image", e);
        }
    }

}
