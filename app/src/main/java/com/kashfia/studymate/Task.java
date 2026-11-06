package com.kashfia.studymate;

public class Task {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private String taskDate;
    private String userId;
    private boolean isCompleted;
    private String priority; // Task priority (High, Medium, Low)

    // Empty constructor required for Firestore
    public Task() {
    }

    public Task(String taskId, String taskTitle, String taskDescription, String taskDate, String userId, boolean isCompleted, String priority) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
        this.userId = userId;
        this.isCompleted = isCompleted;
        this.priority = priority;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}