package com.AgsCh.task_scheduler.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;

import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.UserRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;
        private final UserRepository userRepository;

        public SecurityConfig(CustomUserDetailsService userDetailsService, UserRepository userRepository) {
                this.userDetailsService = userDetailsService;
                this.userRepository = userRepository;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .userDetailsService(userDetailsService)

                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/h2-console/**", "/api/**"))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/login",
                                                                "/forgot-password",
                                                                "/reset-password",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/h2-console/**")
                                                .permitAll()

                                                .requestMatchers("/change-password").authenticated()

                                                .requestMatchers("/webmaster/**").hasRole("WEBMASTER")
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/user/**").hasRole("USER")

                                                .anyRequest().authenticated())

                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()))

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .successHandler((request, response, authentication) -> {

                                                        var principal = (org.springframework.security.core.userdetails.User) authentication
                                                                        .getPrincipal();

                                                        String username = principal.getUsername();

                                                        User user = userRepository.findByUsername(username)
                                                                        .orElseThrow();

                                                        // 🔐 FORZAR CAMBIO DE CONTRASEÑA
                                                        if (user.isPasswordTemporary()) {
                                                                response.sendRedirect("/change-password");
                                                                return;
                                                        }

                                                        var authorities = authentication.getAuthorities();

                                                        if (authorities.stream().anyMatch(a -> a.getAuthority()
                                                                        .equals("ROLE_WEBMASTER"))) {
                                                                response.sendRedirect("/webmaster/dashboard");

                                                        } else if (authorities.stream().anyMatch(
                                                                        a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                                                                response.sendRedirect("/admin/dashboard");

                                                        } else if (authorities.stream().anyMatch(
                                                                        a -> a.getAuthority().equals("ROLE_USER"))) {
                                                                response.sendRedirect("/user");

                                                        } else {
                                                                response.sendRedirect("/");
                                                        }
                                                })
                                                .permitAll())

                                .rememberMe(remember -> remember
                                                .rememberMeParameter("remember-me")
                                                .tokenValiditySeconds(60 * 60 * 24 * 30)
                                                .userDetailsService(userDetailsService))

                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout=true"));

                return http.build();
        }
}