package com.volnoor.githubclient;

/**
 * Created by Eugene on 17.08.2017.
 */

public class Repository {
    private String name;
    private String description;

    public Repository(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
