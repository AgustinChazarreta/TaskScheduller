package com.AgsCh.task_scheduler.service.admin;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.port.external.ImageStorageService;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {

    @Override
    public String upload(MultipartFile file) {
        // implementación real o mock
        return "/uploads/" + file.getOriginalFilename();
    }
}