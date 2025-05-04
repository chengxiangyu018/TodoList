package com.example.todolist;

import java.util.Arrays;
import java.util.List;

public class Category {
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    private String name;
    private String color;

    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public static List<Category> getDefaultCategories() {
        return Arrays.asList(
                new Category("Work", "#FF5722"),
                new Category("Studying", "#4CAF50"),
                new Category("Life", "#2196F3")
        );
    }
}
