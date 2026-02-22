package com.AgsCh.task_scheduler.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!userRepository.existsByRole(Role.WEBMASTER)) {

            User webmaster = new User();
            webmaster.setUsername("webmaster");
            webmaster.setPassword(passwordEncoder.encode("admin123"));
            webmaster.setRole(Role.WEBMASTER);
            webmaster.setActive(true);

            userRepository.save(webmaster);

            System.out.println("=========================================");
            System.out.println("WEBMASTER CREATED");
            System.out.println("username: webmaster");
            System.out.println("password: admin123");
            System.out.println("CHANGE THIS PASSWORD IMMEDIATELY");
            System.out.println("=========================================");
        }
    }
}
