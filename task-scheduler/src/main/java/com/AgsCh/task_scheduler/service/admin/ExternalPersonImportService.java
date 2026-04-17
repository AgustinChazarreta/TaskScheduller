package com.AgsCh.task_scheduler.service.admin;

import com.AgsCh.task_scheduler.dto.external.ExternalPersonDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.port.external.ExternalPersonSearchPort;
import com.AgsCh.task_scheduler.port.external.ImageStorageService;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.service.domain.CurrentUserService;
import com.AgsCh.task_scheduler.util.file.ByteArrayMultipartFile;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import java.util.List;

@Service
public class ExternalPersonImportService {

    private final ExternalPersonSearchPort externalPort;
    private final PersonRepository personRepository;
    private final CurrentUserService currentUserService;
    private final ImageStorageService imageStorageService;
    private final Map<String, List<ExternalPersonDTO>> cache = new ConcurrentHashMap<>();

    public ExternalPersonImportService(
            ExternalPersonSearchPort externalPort,
            PersonRepository personRepository,
            CurrentUserService currentUserService,
            ImageStorageService imageStorageService) {

        this.externalPort = externalPort;
        this.personRepository = personRepository;
        this.currentUserService = currentUserService;
        this.imageStorageService = imageStorageService;
    }

    // 🔍 solo búsqueda
    public List<ExternalPersonDTO> search(String name) {

        if (name == null || name.trim().length() < 3) {
            return List.of();
        }

        String key = name.trim().toLowerCase();

        // =========================
        // 1. CACHE HIT
        // =========================
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        // =========================
        // 2. FETCH EXTERNAL
        // =========================
        List<ExternalPersonDTO> external = externalPort.searchByName(name);

        List<String> ordensPermitidas = currentUserService.getCurrentUserOrdens();

        // =========================
        // 3. FILTERING
        // =========================
        List<ExternalPersonDTO> result = external.stream()
                .filter(dto -> dto.getEmail() != null)
                .filter(dto -> !personRepository.existsByEmail(dto.getEmail()))
                .filter(dto -> dto.getOrden() != null)
                .filter(dto -> ordensPermitidas.contains(dto.getOrden()))
                .toList();

        // =========================
        // 4. CACHE STORE
        // =========================
        cache.put(key, result);

        return result;
    }

    // 📥 importar al sistema interno
    public Person importPerson(ExternalPersonDTO dto) {

        if (dto.getEmail() == null || dto.getFullName() == null) {
            throw new RuntimeException("Datos incompletos");
        }

        if (personRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Persona ya existe");
        }

        Person person = new Person();
        person.setFullName(dto.getFullName());
        person.setEmail(dto.getEmail());
        person.setActive(true);
        // 🔥 FOTO
        if (dto.getPhoto() != null) {

            MultipartFile file = new ByteArrayMultipartFile(
                    dto.getPhoto(),
                    "external-photo.jpg",
                    "image/jpeg");

            String url = imageStorageService.upload(file);

            person.setProfileImageUrl(url);
        }

        return personRepository.save(person);
    }
}