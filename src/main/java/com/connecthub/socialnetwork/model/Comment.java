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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Comment {

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
}

