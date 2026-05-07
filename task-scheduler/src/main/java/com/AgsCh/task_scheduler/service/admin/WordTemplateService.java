package com.AgsCh.task_scheduler.service.admin;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.WordTemplate;
import com.AgsCh.task_scheduler.repository.WordTemplateRepository;
import java.io.IOException;

@Service
public class WordTemplateService {

    private final WordTemplateRepository repository;

    public WordTemplateService(WordTemplateRepository repository) {
        this.repository = repository;
    }

    public void saveTemplate(MultipartFile file,
            House house) throws IOException {

        repository.findByHouseId(house.getId())
                .ifPresent(repository::delete);

        WordTemplate template = new WordTemplate(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                house);

        repository.save(template);
    }

    public WordTemplate getTemplate(Long houseId) {

        return repository.findByHouseId(houseId)
                .orElseThrow(() -> new RuntimeException("No hay template Word cargado"));
    }
}