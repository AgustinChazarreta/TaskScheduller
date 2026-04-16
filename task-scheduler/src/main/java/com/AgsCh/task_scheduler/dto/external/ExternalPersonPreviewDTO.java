package com.AgsCh.task_scheduler.dto.external;

public class ExternalPersonPreviewDTO {

    private String fullName;
    private String email;
    private String orden;
    private String birthDate;
    private String photoBase64;

    public ExternalPersonPreviewDTO(String fullName, String email, String orden, String birthDate, String photoBase64) {
        this.fullName = fullName;
        this.email = email;
        this.orden = orden;
        this.birthDate = birthDate;
        this.photoBase64 = photoBase64;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
    public String getOrden() {
        return orden;
    }

    public String getBirthDate() {
        return birthDate;
    }
    public String getPhotoBase64() {
        return photoBase64;
    }
}
