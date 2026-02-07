package com.gob.small.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gob.small.model.Project;

@Service
public class ProjectService {
    public List<Project> getAll() {
        Project project1 = new Project(
                "ElixirDCl",
                "Its a DCL in Elixir made write Home Assistant configurations easier",
                "http: github/in_a_near_future"
        );
        Project project2 = new Project(
                "AP",
                "This project has a few games that I have developed, one of them is the impostor game which is really popular in Spain",
                "https://github.com/underBalance/AP"
        );

        return List.of(project1, project2);
    }
}
