package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AgsCh.task_scheduler.controller.auth.AdminRequest;
import com.AgsCh.task_scheduler.controller.auth.HouseRequest;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.HouseService;
import com.AgsCh.task_scheduler.service.admin.UserService;

@RestController
@RequestMapping("/webmaster-rest")
@PreAuthorize("hasRole('WEBMASTER')")
public class WebmasterController {

    private final HouseService houseService;
    private final UserService userService;

    public WebmasterController(HouseService houseService,
            UserService userService) {
        this.houseService = houseService;
        this.userService = userService;
    }

    @PostMapping("/houses")
    public House createHouse(@RequestBody HouseRequest request) {
        return houseService.createHouse(request.getName(), request.isActive());
    }

    @PostMapping("/houses/{houseId}/admins")
    public User createAdmin(@PathVariable Long houseId,
            @RequestBody AdminRequest request) {

        return userService.createAdmin(
                houseId,
                request.getUsername(),
                request.getPassword());
    }

    @GetMapping("/houses")
    public List<House> getAllHouses() {
        return houseService.getAllHouses();
    }

}
