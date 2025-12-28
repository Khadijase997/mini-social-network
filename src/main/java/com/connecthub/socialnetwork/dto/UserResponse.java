package com.connecthub.socialnetwork.dto;

public class UserResponse {

    private String id;
    private String name;
    private String email;
    private String bio;
    private String photoUrl;
    private int friendsCount;

    public UserResponse() {
    }

    public UserResponse(String id, String name, String email, String bio, String photoUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.photoUrl = photoUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }
}