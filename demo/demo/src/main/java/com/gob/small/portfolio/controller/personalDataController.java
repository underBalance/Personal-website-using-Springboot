package com.gob.small.portfolio.controller;



import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gob.small.portfolio.model.Personal;
import com.gob.small.portfolio.service.PersonalService;


@RestController
@RequestMapping("/api/v1/personal_data")
public class personalDataController {
    private final PersonalService personalService;

    public personalDataController(PersonalService personalService) {
        this.personalService = personalService;
    }

    @GetMapping
    public List<Personal> getPersonal() {
        return personalService.getAll();
    }
}
