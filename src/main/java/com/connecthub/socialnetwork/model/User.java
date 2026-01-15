package com.connecthub.socialnetwork.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;
@Node("User")
public class User {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    private String name;
    private String email;
    private String password;
    private String bio;
    private LocalDateTime createdAt;
    private String profileImage;

    // Intérêts de l'utilisateur (pour recommandations et recherche)
    private Set<String> interests = new HashSet<>();

    // Liens externes (visibles uniquement pour les amis)
    private String whatsappLink;
    private String instagramLink;
    private String messengerLink;

    // Amis (relation bidirectionnelle)
    @Relationship(type = "CONNECTED_TO", direction = Relationship.Direction.OUTGOING)
    private Set<User> friends = new HashSet<>();

    // Demandes d'amis envoyées
    @Relationship(type = "FRIEND_REQUEST", direction = Relationship.Direction.OUTGOING)
    private Set<User> sentFriendRequests = new HashSet<>();

    // Demandes d'amis reçues
    @Relationship(type = "FRIEND_REQUEST", direction = Relationship.Direction.INCOMING)
    private Set<User> receivedFriendRequests = new HashSet<>();

    // Utilisateurs bloqués
    @Relationship(type = "BLOCKED", direction = Relationship.Direction.OUTGOING)
    private Set<User> blockedUsers = new HashSet<>();

    // Publications créées
    @Relationship(type = "POSTED", direction = Relationship.Direction.OUTGOING)
    private Set<Post> posts = new HashSet<>();

    // Publications likées
    @Relationship(type = "LIKED_BY", direction = Relationship.Direction.OUTGOING)
    private Set<Post> likedPosts = new HashSet<>();

    // Commentaires créés
    @Relationship(type = "COMMENTED", direction = Relationship.Direction.OUTGOING)
    private Set<Comment> comments = new HashSet<>();

    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public Set<String> getInterests() { return interests; }
    public void setInterests(Set<String> interests) { this.interests = interests; }
    public String getWhatsappLink() { return whatsappLink; }
    public void setWhatsappLink(String whatsappLink) { this.whatsappLink = whatsappLink; }
    public String getInstagramLink() { return instagramLink; }
    public void setInstagramLink(String instagramLink) { this.instagramLink = instagramLink; }
    public String getMessengerLink() { return messengerLink; }
    public void setMessengerLink(String messengerLink) { this.messengerLink = messengerLink; }
    public Set<User> getFriends() { return friends; }
    public void setFriends(Set<User> friends) { this.friends = friends; }
    public Set<User> getSentFriendRequests() { return sentFriendRequests; }
    public void setSentFriendRequests(Set<User> sentFriendRequests) { this.sentFriendRequests = sentFriendRequests; }
    public Set<User> getReceivedFriendRequests() { return receivedFriendRequests; }
    public void setReceivedFriendRequests(Set<User> receivedFriendRequests) { this.receivedFriendRequests = receivedFriendRequests; }
    public Set<User> getBlockedUsers() { return blockedUsers; }
    public void setBlockedUsers(Set<User> blockedUsers) { this.blockedUsers = blockedUsers; }
    public Set<Post> getPosts() { return posts; }
    public void setPosts(Set<Post> posts) { this.posts = posts; }
    public Set<Post> getLikedPosts() { return likedPosts; }
    public void setLikedPosts(Set<Post> likedPosts) { this.likedPosts = likedPosts; }
    public Set<Comment> getComments() { return comments; }
    public void setComments(Set<Comment> comments) { this.comments = comments; }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
