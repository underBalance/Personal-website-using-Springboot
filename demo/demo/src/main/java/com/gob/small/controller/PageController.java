package com.gob.small.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gob.small.service.PersonalService;
import com.gob.small.service.ProjectService;
import com.gob.small.service.ToDoService;

@Controller
public class PageController {
    private final PersonalService personalService;
    private final ProjectService projectService;
    private final ToDoService toDoService;

    public PageController(
            PersonalService personalService,
            ProjectService projectService,
            ToDoService toDoService
    ) {
        this.personalService = personalService;
        this.projectService = projectService;
        this.toDoService = toDoService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("personal", personalService.getAll());
        model.addAttribute("projects", projectService.getAll());
        model.addAttribute("todos", toDoService.getAll());
        return "index";
    }
}
