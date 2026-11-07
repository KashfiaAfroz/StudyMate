package com.kashfia.studymate;

public class Exam {
    private String title;
    private String date;

    public Exam(String title, String date) {
        this.title = title;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
}