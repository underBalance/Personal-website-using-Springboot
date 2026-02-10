package com.gob.small.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gob.small.portfolio.model.Personal;

@Service
public class PersonalService {
    public List<Personal> getAll() {
        Personal personal1 = new Personal(
                "Gabriel",
                "Junior",
                "2002",
                "I am a software engineer interested in new job opportunities and continuously improving my skills. "
                        + "I focus on designing and developing systems in an ever-evolving software engineering landscape, "
                        + "adapting my technical stack to new challenges and emerging technologies.",
                List.of("https://www.linkedin.com/in/gabriel-olmedilla-barrientos-72187b306/")
        );

        return List.of(personal1);
    }
}
