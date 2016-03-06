package com.hackathon.igfeels.instagramApi;

public class UserEntry {
    private String username;
    private String id;
    private String bio;

    public UserEntry(String username, String id, String bio) {
        this.username = username;
        this.id = id;
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public String getBio(){
        return bio;
    }
}
