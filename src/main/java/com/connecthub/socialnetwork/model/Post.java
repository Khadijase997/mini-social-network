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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

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
}