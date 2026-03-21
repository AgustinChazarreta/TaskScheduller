package com.AgsCh.task_scheduler.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgsCh.task_scheduler.model.PendingAdminRegistration;

public interface PendingAdminRegistrationRepository
        extends JpaRepository<PendingAdminRegistration, Long> {

    Optional<PendingAdminRegistration> findByToken(String token);

    boolean existsByEmail(String email);
}