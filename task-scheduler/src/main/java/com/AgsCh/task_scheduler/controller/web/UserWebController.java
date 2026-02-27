package com.AgsCh.task_scheduler.controller.web;

import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.service.admin.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('USER')")
public class UserWebController {

    private final UserService userService;

    public UserWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public String userDashboard(Model model) {
        addUserAttributes(model);
        return "user/dashboard";
    }

    @GetMapping("/user/results")
    public String userResults(Model model) {
        addUserAttributes(model);
        return "user/results";
    }

    private void addUserAttributes(Model model) {
        User user = userService.getAuthenticatedUser();

        String fullName = user.getPerson().getFullName();

        String houseName = user.getHouse() != null
                ? user.getHouse().getName()
                : "";

        model.addAttribute("userName", fullName);
        model.addAttribute("houseName", houseName);
    }
}