package org.example;

import java.util.ArrayList;
import java.util.List;


import java.util.List;
import java.util.ArrayList;

public class User {
    private String username;
    private String role;
    private List<Task> tasks;

    public User() {
        this.tasks = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<Task> getTasks() { return tasks != null ? tasks : new ArrayList<>(); }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }
}