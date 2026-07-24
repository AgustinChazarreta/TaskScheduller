package com.AgsCh.task_scheduler.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;

import com.AgsCh.task_scheduler.model.User;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;
        private final com.AgsCh.task_scheduler.session.AdminSession adminSession;

        public SecurityConfig(
                        CustomUserDetailsService userDetailsService,
                        com.AgsCh.task_scheduler.session.AdminSession adminSession) {

                this.userDetailsService = userDetailsService;
                this.adminSession = adminSession;
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
                                                .ignoringRequestMatchers("/h2-console/**", "/api/**",
                                                                "/auth/register/admin")) // <-- agregar

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/login",
                                                                "/forgot-password",
                                                                "/reset-password",
                                                                "/auth/register/**",
                                                                "/auth/verify",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/h2-console/**")
                                                .permitAll()

                                                .requestMatchers("/change-password").authenticated()

                                                .requestMatchers("/webmaster/**").hasRole("WEBMASTER")
                                                .requestMatchers("/admin/**")
                                                .access((authentication, context) -> {

                                                        boolean isAdmin = authentication.get()
                                                                        .getAuthorities()
                                                                        .stream()
                                                                        .anyMatch(a -> a.getAuthority()
                                                                                        .equals("ROLE_ADMIN"));

                                                        boolean isWebmaster = authentication.get()
                                                                        .getAuthorities()
                                                                        .stream()
                                                                        .anyMatch(a -> a.getAuthority()
                                                                                        .equals("ROLE_WEBMASTER"));

                                                        boolean impersonating = adminSession.isImpersonating();

                                                        return new AuthorizationDecision(
                                                                        isAdmin || (isWebmaster && impersonating));
                                                })
                                                .requestMatchers("/user/**").hasRole("USER")

                                                .anyRequest().authenticated())

                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()))

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .failureHandler((request, response, exception) -> {

                                                        if (exception instanceof org.springframework.security.authentication.DisabledException) {
                                                                response.sendRedirect("/login?disabled=true");
                                                        } else {
                                                                response.sendRedirect("/login?error=true");
                                                        }
                                                })
                                                .successHandler((request, response, authentication) -> {

                                                        CustomUserDetails principal = (CustomUserDetails) authentication
                                                                        .getPrincipal();

                                                        User user = principal.getUser();

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
                                                }).permitAll())

                                .rememberMe(remember -> remember.rememberMeParameter("remember-me")
                                                .tokenValiditySeconds(60 * 60 * 24 * 30)
                                                .userDetailsService(userDetailsService))

                                .logout(logout -> logout.logoutSuccessUrl("/login?logout=true"));

                return http.build();
        }
}