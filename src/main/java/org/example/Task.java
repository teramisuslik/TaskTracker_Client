package org.example;

// Временный класс Task для демонстрации
public class Task {
    private String title;
    private String description;
    private String status;
    private String importance;
    private String deadline;

    public Task(String title, String description, String status, String importance, String deadline) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.importance = importance;
        this.deadline = deadline;
    }

    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getImportance() { return importance; }
    public String getDeadline() { return deadline; }
}
