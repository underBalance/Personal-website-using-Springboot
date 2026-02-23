package com.gob.small.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gob.small.portfolio.model.ToDo;

@Service
public class ToDoService {
    public List<ToDo> getAll() {
        ToDo todo1 = new ToDo(
                "Build portfolio",
                "Create a personal portfolio site with Spring Boot backend.",
                List.of("spring-boot", "portfolio", "backend")
        );

        ToDo todo2 = new ToDo(
                "Try webscrapping",
                "I want to create a feature here that uses web scrapping so that I can learn a little inside this area.",
                List.of("webScrapping", "java", "python")
        );

        return List.of(todo1, todo2);
    }
}
