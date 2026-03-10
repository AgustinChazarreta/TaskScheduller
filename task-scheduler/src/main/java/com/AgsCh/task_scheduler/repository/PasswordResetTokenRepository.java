package com.AgsCh.task_scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.AgsCh.task_scheduler.model.PasswordResetToken;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);
}