package com.connecthub.socialnetwork.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;

/**
 * Modèle représentant un commentaire sur une publication
 * Relations Neo4j:
 * - User -[COMMENTED]-> Comment
 * - Comment -[ON_POST]-> Post
 */
@Node("Comment")
public class Comment {

    public Comment() {}

    public Comment(Long id, String content, LocalDateTime createdAt, User author, Post post) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.author = author;
        this.post = post;
    }

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    private String content;
    private LocalDateTime createdAt;

    /**
     * Auteur du commentaire
     * Relation: User -[COMMENTED]-> Comment
     */
    @Relationship(type = "COMMENTED", direction = Relationship.Direction.INCOMING)
    private User author;

    /**
     * Publication sur laquelle ce commentaire a été posté
     * Relation: Comment -[ON_POST]-> Post
     */
    @Relationship(type = "ON_POST", direction = Relationship.Direction.OUTGOING)
    private Post post;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
}

