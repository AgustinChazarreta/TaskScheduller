package com.AgsCh.task_scheduler.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.AgsCh.task_scheduler.session.AdminSession;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminSessionInterceptor implements HandlerInterceptor {

    private final AdminSession adminSession;

    public AdminSessionInterceptor(AdminSession adminSession) {
        this.adminSession = adminSession;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        request.setAttribute(
                "impersonating",
                adminSession.isImpersonating());

        request.setAttribute(
                "impersonatedHouseName",
                adminSession.getHouseName());

        return true;
    }
}