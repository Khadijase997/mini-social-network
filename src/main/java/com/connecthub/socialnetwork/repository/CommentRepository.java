package com.connecthub.socialnetwork.repository;

import com.connecthub.socialnetwork.model.Comment;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository pour les commentaires avec requêtes Cypher optimisées
 */
public interface CommentRepository extends Neo4jRepository<Comment, Long> {

    /**
     * Récupère tous les commentaires d'une publication
     * Ordre chronologique (plus ancien en premier)
     */
    @Query("""
        MATCH (p:Post {id: $postId})-[:HAS_COMMENT]<-(:Comment)<-[:COMMENTED]-(author:User)
        MATCH (c:Comment)-[:ON_POST]->(p)
        RETURN c, author
        ORDER BY c.createdAt ASC
    """)
    List<Comment> findCommentsByPostId(@Param("postId") Long postId);

    /**
     * Compte le nombre de commentaires d'une publication
     */
    @Query("""
        MATCH (p:Post {id: $postId})-[:HAS_COMMENT]->(c:Comment)
        RETURN count(c)
    """)
    int countCommentsByPostId(@Param("postId") Long postId);
}

