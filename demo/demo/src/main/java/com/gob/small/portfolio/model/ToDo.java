package com.gob.small.portfolio.model;

import java.util.List;

public class ToDo {
    private String name;
    private String description;
    private List<String> tags;

    public ToDo(String name, String description, List<String> tags) {
        this.name = name;
        this.description = description;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }
}
