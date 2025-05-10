package com.example.todolist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private boolean isCompleted;
    private String category;
    private int priority;

    public Task(String title, String description, String dueDate) {
        this(title, description, dueDate, null, 0);
    }

    public Task(String title, String description, String dueDate, String category, int priority) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.category = category;
        this.priority = priority;
        this.isCompleted = false;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }
    public boolean isCompleted() { return isCompleted; }
    public String getCategory() { return category; }
    public int getPriority() { return priority; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public void setCategory(String category) { this.category = category; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getFormattedDueDate() {
        if (dueDate == null || dueDate.isEmpty()) {
            return "No deadline";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dueDate);
            return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return dueDate;
        }
    }

    public boolean isOverdue() {
        if (isCompleted || dueDate == null || dueDate.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date due = sdf.parse(dueDate);
            return due.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    public String getPriorityText() {
        switch (priority) {
            case 1: return "❗High";
            case 2: return "⚠️Medium";
            default: return "⚪Normal";
        }
    }
}
