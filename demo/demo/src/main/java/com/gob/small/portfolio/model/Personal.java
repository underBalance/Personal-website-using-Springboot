package com.gob.small.portfolio.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "role", "year", "summary", "links" })
public class Personal {
    
    private String name;
    private String role;
    private String year;
    private String summary;
    private List<String> links;

    public Personal(String name, String role, String year, String summary, List<String> links) {
        this.name = name;
        this.role = role;
        this.year = year;
        this.summary =  summary;
        this.links = links;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getYear() {
        return year;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getLinks() {
        return links;
    }

}
