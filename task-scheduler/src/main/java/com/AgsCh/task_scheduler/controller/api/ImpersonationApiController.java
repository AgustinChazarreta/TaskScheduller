package com.AgsCh.task_scheduler.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.service.admin.HouseService;
import com.AgsCh.task_scheduler.session.AdminSession;
import com.AgsCh.task_scheduler.repository.UserRepository;
import com.AgsCh.task_scheduler.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/impersonation")
@PreAuthorize("hasRole('WEBMASTER')")
public class ImpersonationApiController {

        private final AdminSession adminSession;
        private final HouseService houseService;
        private final UserRepository userRepository;

        public ImpersonationApiController(
                        AdminSession adminSession,
                        HouseService houseService,
                        UserRepository userRepository) {

                this.adminSession = adminSession;
                this.houseService = houseService;
                this.userRepository = userRepository;
        }

        @PostMapping("/house/{houseId}")
        public ResponseEntity<Void> enterHouse(
                        @PathVariable Long houseId) {

                House house = houseService.getHouseById(houseId);

                if (house == null) {
                        return ResponseEntity.notFound().build();
                }

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                User webmaster = userRepository.findByUsername(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("Webmaster not found"));

                // asignación temporal
                webmaster.setHouse(house);

                userRepository.save(webmaster);

                System.out.println(
                                "WEBMASTER "
                                                + webmaster.getUsername()
                                                + " ahora tiene house "
                                                + house.getId());

                adminSession.setHouse(house);
                adminSession.setHouseId(house.getId());
                adminSession.setHouseName(house.getName());
                adminSession.setImpersonating(true);

                System.out.println(
                                "Webmaster entrando en House: "
                                                + house.getName());

                return ResponseEntity.ok().build();
        }

        @GetMapping("/current")
        public ResponseEntity<?> currentHouse() {

                return ResponseEntity.ok(new Object() {

                        public final Long houseId = adminSession.getHouseId();
                        public final String houseName = adminSession.getHouseName();
                        public final boolean impersonating = adminSession.isImpersonating();

                });
        }

        @PostMapping("/stop")
        public ResponseEntity<Void> stopImpersonation(
                        Authentication authentication) {

                User webmaster = userRepository.findByUsername(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("Webmaster not found"));

                // volver al estado original
                webmaster.setHouse(null);

                userRepository.save(webmaster);

                System.out.println(
                                "WEBMASTER restaurado. House = null");

                adminSession.clear();

                return ResponseEntity.ok().build();
        }
}