package com.gob.small.portfolio.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gob.small.portfolio.model.ToDo;
import com.gob.small.portfolio.service.ToDoService;

@RestController
@RequestMapping("/api/v1/todos")
public class ToDoController {
    private final ToDoService toDoService;

    public ToDoController(ToDoService toDoService) {
        this.toDoService = toDoService;
    }

    @GetMapping
    public List<ToDo> getTodos() {
        return toDoService.getAll();
    }
}
