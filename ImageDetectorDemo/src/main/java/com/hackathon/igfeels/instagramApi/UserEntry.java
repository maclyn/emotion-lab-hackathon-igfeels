package com.hackathon.igfeels.instagramApi;

public class UserEntry {
    private String username;
    private int id;
    private String bio;

    public UserEntry(String username, int id, String bio) {
        this.username = username;
        this.id = id;
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }

    public String getBio(){
        return bio;
    }
}
