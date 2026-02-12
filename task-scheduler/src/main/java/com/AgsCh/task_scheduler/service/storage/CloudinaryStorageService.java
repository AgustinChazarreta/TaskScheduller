package com.AgsCh.task_scheduler.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryStorageService implements FileStorageService {

    private final Cloudinary cloudinary;
    private String lastUploadedPublicId; // guardamos el publicId

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /*
     * @SuppressWarnings("unchecked")
     * 
     * @Override
     * public String uploadFile(MultipartFile file) {
     * try {
     * 
     * String publicId = UUID.randomUUID().toString();
     * 
     * Map<String, Object> uploadResult = (Map<String, Object>)
     * cloudinary.uploader().upload(
     * file.getBytes(),
     * ObjectUtils.asMap(
     * "public_id", publicId,
     * "folder", "profile_images"));
     * 
     * Object url = uploadResult.get("secure_url");
     * 
     * if (url == null) {
     * throw new RuntimeException("Cloudinary did not return secure_url");
     * }
     * 
     * return url.toString();
     * 
     * } catch (IOException e) {
     * throw new RuntimeException("Error uploading file to Cloudinary", e);
     * }
     * }
     */

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String publicId = UUID.randomUUID().toString();

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "profile_images"
                    ));

            Object url = uploadResult.get("secure_url");
            if (url == null) {
                throw new RuntimeException("Cloudinary did not return secure_url");
            }

            lastUploadedPublicId = uploadResult.get("public_id").toString();
            return url.toString();

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to Cloudinary", e);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file from Cloudinary", e);
        }
    }

    @Override
    public String getLastUploadedPublicId() {
        return lastUploadedPublicId;
    }
}

