package com.AgsCh.task_scheduler.port.external;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String upload(MultipartFile file);
}
