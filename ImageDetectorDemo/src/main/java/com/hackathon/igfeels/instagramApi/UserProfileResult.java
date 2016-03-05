package com.hackathon.igfeels.instagramApi;

public class UserProfileResult {
    private UserEntry data;

    public UserProfileResult(UserEntry data) {
        this.data = data;
    }

    public UserEntry getData() {
        return data;
    }
}
