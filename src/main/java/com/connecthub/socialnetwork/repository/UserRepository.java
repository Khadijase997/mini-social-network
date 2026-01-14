package com.connecthub.socialnetwork.repository;

import com.connecthub.socialnetwork.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour les utilisateurs avec requêtes Cypher optimisées
 */
@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    Optional<User> findByEmail(String email);

    /**
     * Recherche d'utilisateurs par nom (insensible à la casse)
     */
    @Query("MATCH (u:User) WHERE toLower(u.name) CONTAINS toLower($name) RETURN u LIMIT 20")
    List<User> searchByName(String name);

    /**
     * Compte le nombre d'amis d'un utilisateur
     */
    @Query("MATCH (u:User {id: $userId})-[:CONNECTED_TO]-(friend) RETURN COUNT(friend)")
    int countFriends(String userId);

    /**
     * Recherche avancée par nom, email, bio et intérêts
     * Utilise CONTAINS pour une recherche flexible
     */
    @Query("""
        MATCH (u:User)
        WHERE toLower(u.name) CONTAINS toLower($keyword)
           OR toLower(u.email) CONTAINS toLower($keyword)
           OR toLower(u.bio) CONTAINS toLower($keyword)
           OR any(interest IN u.interests WHERE toLower(interest) CONTAINS toLower($keyword))
        RETURN u
        LIMIT 50
    """)
    List<User> searchUsers(@Param("keyword") String keyword);

    /**
     * Recherche par intérêts partagés
     * Retourne les utilisateurs ayant au moins un intérêt en commun
     */
    @Query("""
        MATCH (current:User {id: $userId})
        MATCH (other:User)
        WHERE other.id <> $userId
          AND any(interest IN current.interests WHERE interest IN other.interests)
          AND NOT (current)-[:BLOCKED]->(other)
          AND NOT (other)-[:BLOCKED]->(current)
        RETURN other, size([i IN current.interests WHERE i IN other.interests]) as commonInterests
        ORDER BY commonInterests DESC
        LIMIT $limit
    """)
    List<User> findUsersBySharedInterests(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * Compte le nombre de publications d'un utilisateur
     */
    @Query("""
        MATCH (u:User {id: $userId})-[:POSTED]->(p:Post)
        RETURN count(p)
    """)
    int countPosts(String userId);

    /**
     * Compte le nombre total de likes reçus par un utilisateur
     */
    @Query("""
        MATCH (u:User {id: $userId})-[:POSTED]->(p:Post)<-[:LIKED_BY]-(liker:User)
        RETURN count(liker)
    """)
    int countTotalLikesReceived(String userId);

    /**
     * Récupère un sous-ensemble d'utilisateurs (sans filtre), limité
     * Utilisé pour l'exploration /search sans surcharger le graphe complet.
     */
    @Query("""
        MATCH (u:User)
        RETURN u
        LIMIT $limit
    """)
    List<User> findSomeUsers(@Param("limit") int limit);
}