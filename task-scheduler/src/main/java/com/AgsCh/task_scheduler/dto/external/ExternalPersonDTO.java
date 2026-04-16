package com.AgsCh.task_scheduler.dto.external;

public class ExternalPersonDTO {

    private String fullName;
    private String email;
    private String orden;
    private String birthDate;
    private byte[] photo;

    public ExternalPersonDTO() {
    }

    public ExternalPersonDTO(String fullName, String email, String orden, String birthDate, byte[] photo) {
        this.fullName = fullName;
        this.email = email;
        this.orden = orden;
        this.birthDate = birthDate;
        this.photo = photo;
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

    public byte[] getPhoto() {
        return photo;
    }
}