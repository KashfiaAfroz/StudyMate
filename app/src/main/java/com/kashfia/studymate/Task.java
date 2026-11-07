package com.kashfia.studymate;

public class Task {
    private String taskId;
    private String userId;
    private String taskTitle;
    private String taskDescription;
    private String taskDate;
    private String priority;
    private boolean completed;

    public Task() {}

    public Task(String taskId, String userId, String taskTitle, String taskDescription, String taskDate, String priority, boolean completed) {
        this.taskId = taskId;
        this.userId = userId;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
        this.priority = priority;
        this.completed = completed;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public String getTaskDate() { return taskDate; }
    public void setTaskDate(String taskDate) { this.taskDate = taskDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}