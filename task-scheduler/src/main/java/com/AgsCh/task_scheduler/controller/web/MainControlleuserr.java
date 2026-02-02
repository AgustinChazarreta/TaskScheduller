package com.AgsCh.task_scheduler.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainControlleuserr {

    @GetMapping({ "/user" })
    public String dashboard() {
        return "index";
    }
}
