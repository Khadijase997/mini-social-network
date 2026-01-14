package com.connecthub.socialnetwork.repository;

import com.connecthub.socialnetwork.model.Post;
import com.connecthub.socialnetwork.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository pour les publications avec requêtes Cypher optimisées
 */
public interface PostRepository extends Neo4jRepository<Post, Long> {

    /**
     * Récupère le feed des amis de l'utilisateur connecté
     * Utilise une traversée de graphe: User -> CONNECTED_TO -> User -> POSTED ->
     * Post
     * Exclut les utilisateurs bloqués
     */
    @Query("""
                MATCH (me:User {name: $username})-[:CONNECTED_TO]->(f:User)
                WHERE NOT (me)-[:BLOCKED]->(f) AND NOT (f)-[:BLOCKED]->(me)
                MATCH (f)-[:POSTED]->(p:Post)
                WITH p, f
                ORDER BY p.createdAt DESC
                LIMIT 50
                OPTIONAL MATCH (p)<-[:LIKED_BY]-(liker:User)
                OPTIONAL MATCH (p)-[:HAS_COMMENT]->(c:Comment)
                RETURN p, f, liker, c
            """)
    List<Post> findFeedPosts(@Param("username") String username);

    /**
     * Récupère un feed global pour l'utilisateur connecté
     */
    @Query("""
                MATCH (me:User {email: $email})
                MATCH (author:User)-[:POSTED]->(p:Post)
                WHERE NOT (me)-[:BLOCKED]->(author)
                  AND NOT (author)-[:BLOCKED]->(me)
                WITH p, author
                ORDER BY p.createdAt DESC
                LIMIT 100
                OPTIONAL MATCH (p)<-[:LIKED_BY]-(liker:User)
                OPTIONAL MATCH (p)-[:HAS_COMMENT]->(c:Comment)
                RETURN p, author, liker, c
            """)
    List<Post> findFeedPostsByEmail(@Param("email") String email);

    /**
     * Récupère les publications d'un utilisateur par email
     */
    @Query("""
                MATCH (u:User {email: $email})-[:POSTED]->(p:Post)
                WITH p, u
                ORDER BY p.createdAt DESC
                OPTIONAL MATCH (p)<-[:LIKED_BY]-(liker:User)
                OPTIONAL MATCH (p)-[:HAS_COMMENT]->(c:Comment)
                RETURN p, u, liker, c
            """)
    List<Post> findPostsByUserEmail(@Param("email") String email);

    /**
     * Récupère les publications d'un utilisateur par ID
     */
    @Query("""
                MATCH (u:User {id: $userId})-[:POSTED]->(p:Post)
                WITH p, u
                ORDER BY p.createdAt DESC
                OPTIONAL MATCH (p)<-[:LIKED_BY]-(liker:User)
                OPTIONAL MATCH (p)-[:HAS_COMMENT]->(c:Comment)
                RETURN p, u, liker, c
            """)
    List<Post> findPostsByUserId(@Param("userId") String userId);

    @Query("""
                MATCH (p:Post {id: $postId})
                OPTIONAL MATCH (p)-[:HAS_COMMENT]->(c:Comment)
                DETACH DELETE p, c
            """)
    void deletePostById(@Param("postId") Long postId);

    /**
     * Vérifie si un utilisateur a déjà liké une publication
     */
    @Query("""
                MATCH (u:User {id: $userId})-[:LIKED_BY]->(p:Post {id: $postId})
                RETURN count(u) > 0
            """)
    boolean hasUserLikedPost(@Param("userId") String userId, @Param("postId") Long postId);

    /**
     * Compte le nombre de likes d'une publication
     */
    @Query("""
                MATCH (p:Post {id: $postId})<-[:LIKED_BY]-(liker:User)
                RETURN count(liker) as likeCount
            """)
    int countLikes(@Param("postId") Long postId);

    /**
     * Compte le nombre de commentaires d'une publication
     */
    @Query("""
                MATCH (p:Post {id: $postId})-[:HAS_COMMENT]->(c:Comment)
                RETURN count(c) as commentCount
            """)
    int countComments(@Param("postId") Long postId);

    /**
     * Récupère la liste des utilisateurs qui ont liké une publication
     */
    @Query("""
                MATCH (p:Post {id: $postId})<-[:LIKED_BY]-(u:User)
                RETURN u
                ORDER BY u.name
            """)
    List<User> findLikers(@Param("postId") Long postId);
}