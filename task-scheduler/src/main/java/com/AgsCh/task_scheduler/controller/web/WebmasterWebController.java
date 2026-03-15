package com.AgsCh.task_scheduler.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/webmaster")
@PreAuthorize("hasRole('WEBMASTER')")
public class WebmasterWebController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "webmaster/dashboard";
    }

    @GetMapping("/houses")
    public String houses() {
        return "webmaster/houses";
    }

    @GetMapping("/houses/{id}/admins")
    public String adminsByHouse(@PathVariable Long id, Model model) {

        model.addAttribute("houseId", id);

        return "webmaster/admins-house";
    }

    @GetMapping("/users")
    public String users() {
        return "webmaster/users";
    }

    @GetMapping("/schedules")
    public String schedules() {
        return "webmaster/schedules";
    }

}