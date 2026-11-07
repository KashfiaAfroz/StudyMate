package com.kashfia.studymate;

public class Note {
    private String title;
    private String content;
    private String date;
    private String imageUri;

    // ডিফল্ট কনস্ট্রাক্টর (যদি প্রয়োজন হয়)
    public Note() {}

    public Note(String title, String content, String date, String imageUri) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.imageUri = imageUri;
    }

    // গেটার মেথডসমূহ
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public String getImageUri() { return imageUri; }

    // সেটার মেথডসমূহ (এডিট করার সময় এগুলো কাজ করবে)
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setDate(String date) { this.date = date; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}