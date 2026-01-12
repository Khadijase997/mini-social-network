package com.connecthub.socialnetwork.dto;

public class UserResponse {

    private String id;
    private String name;
    private String email;
    private String bio;
    private int friendsCount;
    private String profileImage;

    public UserResponse() {
    }

    public UserResponse(String id, String name, String email, String bio, String profileImage) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.profileImage = profileImage;
    }

    // ===== GETTERS & SETTERS =====

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

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}