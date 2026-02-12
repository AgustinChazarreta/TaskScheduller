package com.AgsCh.task_scheduler.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Sube un archivo y devuelve la URL segura.
     */
    String uploadFile(MultipartFile file);

    /**
     * Borra un archivo de Cloudinary a partir de su publicId.
     */
    void deleteFile(String publicId);

    /**
     * Devuelve el publicId del último archivo subido.
     */
    String getLastUploadedPublicId();
}
