package org.example;

import java.util.List;
import java.util.ArrayList;

public class Task {
    private String title;
    private String description;
    private String status;
    private String importance;
    private String deadline;
    private List<Comment> comments;

    // Конструктор для парсинга
    public Task(String title, String description, String status, String importance, String deadline) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.importance = importance;
        this.deadline = deadline;
        this.comments = new ArrayList<>();
    }

    // Пустой конструктор
    public Task() {
        this.comments = new ArrayList<>();
    }

    // Геттеры и сеттеры
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public List<Comment> getComments() { return comments != null ? comments : new ArrayList<>(); }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}
