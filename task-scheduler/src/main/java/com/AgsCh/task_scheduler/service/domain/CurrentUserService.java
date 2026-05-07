package com.AgsCh.task_scheduler.service.domain;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.AgsCh.task_scheduler.model.User;

public interface CurrentUserService {

    List<String> getCurrentUserOrdens();

    User getCurrentUser(Authentication authentication);
}