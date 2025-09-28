package org.example;

public class Comment {
    private Long id;
    private String description;
    private Task task;

    public Comment() {}

    public Comment(Long id, String description, Task task) {
        this.id = id;
        this.description = description;
        this.task = task;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}