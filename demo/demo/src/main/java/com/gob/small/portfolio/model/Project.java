package com.gob.small.portfolio.model;

public class Project {
    private String title;
    private String description;
    private String link;

    public Project(String title, String description, String link) {
        this.title = title;
        this.description = description;
        this.link = link;
    }

// For what I have read is a better practice to just use a contructor
/* 
    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setLink(String link){
        this.link = link;
    }
*/   
    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public String getLink(){
        return link;
    }
}
