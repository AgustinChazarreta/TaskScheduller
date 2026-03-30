package com.AgsCh.task_scheduler.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WordEditorController {

    @GetMapping("/admin/schedule/word")
    public String wordEditor() {
        return "word-editor";
    }
}
