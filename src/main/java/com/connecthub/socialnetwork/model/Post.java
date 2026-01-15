package com.connecthub.socialnetwork.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Modèle représentant une publication dans le réseau social
 * Relations Neo4j:
 * - User -[POSTED]-> Post
 * - User -[LIKED_BY]-> Post
 * - User -[COMMENTED_ON]-> Post (via Comment)
 */
@Node("Post")
public class Post {

    public Post() {}

    public Post(Long id, String content, LocalDateTime createdAt, String imageUrl, User author, Set<User> likes, Set<Comment> comments) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.author = author;
        this.likes = likes;
        this.comments = comments;
    }

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    private String content;
    private LocalDateTime createdAt;
    private String imageUrl; // URL de l'image optionnelle

    @Relationship(type = "POSTED", direction = Relationship.Direction.INCOMING)
    private User author;

    /**
     * Utilisateurs qui ont liké cette publication
     * Relation: User -[LIKED_BY]-> Post
     */
    @Relationship(type = "LIKED_BY", direction = Relationship.Direction.INCOMING)
    private Set<User> likes = new HashSet<>();

    /**
     * Commentaires associés à cette publication
     * Relation: Comment -[ON_POST]-> Post
     */
    @Relationship(type = "HAS_COMMENT", direction = Relationship.Direction.OUTGOING)
    private Set<Comment> comments = new HashSet<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Set<User> getLikes() { return likes; }
    public void setLikes(Set<User> likes) { this.likes = likes; }
    public Set<Comment> getComments() { return comments; }
    public void setComments(Set<Comment> comments) { this.comments = comments; }
}