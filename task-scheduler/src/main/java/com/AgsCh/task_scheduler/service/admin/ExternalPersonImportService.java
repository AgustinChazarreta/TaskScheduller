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
import java.util.Set;
import java.util.HashSet;
import java.util.List;

@Service
public class ExternalPersonImportService {

    private final ExternalPersonSearchPort externalPort;
    private final PersonRepository personRepository;
    private final CurrentUserService currentUserService;
    private final ImageStorageService imageStorageService;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private volatile long lastFailureTime = 0;
    private static final long BLOCK_TIME = 60_000; // 1 minuto

    private static class CacheEntry {
        List<ExternalPersonDTO> data;
        long timestamp;

        CacheEntry(List<ExternalPersonDTO> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final long CACHE_TTL = 5 * 60 * 1000; // 5 minutos

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
        CacheEntry entry = cache.get(key);

        if (entry != null && (System.currentTimeMillis() - entry.timestamp) < CACHE_TTL) {
            return entry.data;
        }

        if (System.currentTimeMillis() - lastFailureTime < BLOCK_TIME) {
            return List.of(); // NO intentar conexión
        }

        // =========================
        // 2. FETCH EXTERNAL
        // =========================
        List<ExternalPersonDTO> external;

        try {
            external = externalPort.searchByName(name);
        } catch (Exception e) {
            lastFailureTime = System.currentTimeMillis();
            System.out.println("🚫 DB externa bloqueada temporalmente");
            return List.of();
        }

        List<String> ordensPermitidas = currentUserService.getCurrentUserOrdens();

        // =========================
        // 3. FILTERING (optimizado PRO)
        // =========================

        List<String> emails = external.stream()
                .map(ExternalPersonDTO::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .toList();

        Set<String> existingEmailsSet = emails.isEmpty()
                ? Set.of()
                : new HashSet<>(personRepository.findExistingEmails(emails));

        List<ExternalPersonDTO> result = external.stream()
                .filter(dto -> dto.getEmail() != null && !dto.getEmail().isBlank())
                .filter(dto -> !existingEmailsSet.contains(dto.getEmail()))
                .filter(dto -> dto.getOrden() != null)
                .filter(dto -> ordensPermitidas.contains(dto.getOrden()))
                .toList();
        // =========================
        // 4. CACHE STORE
        // =========================
        cache.put(key, new CacheEntry(result));

        if (cache.size() > 1000) {
            cache.clear(); // simple pero efectivo
        }

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