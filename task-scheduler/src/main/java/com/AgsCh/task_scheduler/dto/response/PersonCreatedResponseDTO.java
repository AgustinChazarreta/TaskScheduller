package com.AgsCh.task_scheduler.dto.response;

public class PersonCreatedResponseDTO {

    private Long personId;
    private String fullName;
    private String email;
    private String temporaryPassword;

    public PersonCreatedResponseDTO(Long personId, String fullName, String email, String temporaryPassword) {
        this.personId = personId;
        this.fullName = fullName;
        this.email = email;
        this.temporaryPassword = temporaryPassword;
    }

    // Getters
    public Long getPersonId() {
        return personId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }
}