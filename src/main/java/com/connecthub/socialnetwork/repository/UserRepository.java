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
     * Retourne les utilisateurs ayant des intérêts en commun, ou simplement
     * d'autres utilisateurs (repli)
     * Exclut soi-même, les amis déjà connectés, les demandes en attente et les
     * bloqués.
     */
    @Query("""
                MATCH (current:User {id: $userId})
                MATCH (other:User)
                WHERE other.id <> $userId
                  AND NOT (current)-[:CONNECTED_TO]-(other)
                  AND NOT (current)-[:FRIEND_REQUEST]-(other)
                  AND NOT (current)-[:BLOCKED]-(other)
                WITH other,
                     size([i IN coalesce(current.interests, []) WHERE i IN coalesce(other.interests, [])]) as commonInterests
                ORDER BY commonInterests DESC, other.name ASC
                RETURN other
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

    @Query("MATCH (sender:User {id: $senderId})-[r:FRIEND_REQUEST]->(receiver:User {id: $receiverId}) DELETE r CREATE (sender)-[:CONNECTED_TO]->(receiver)")
    void acceptFriendRequest(@Param("senderId") String senderId, @Param("receiverId") String receiverId);

    @Query("MATCH (sender:User {id: $senderId})-[r:FRIEND_REQUEST]->(receiver:User {id: $receiverId}) DELETE r")
    void rejectFriendRequest(@Param("senderId") String senderId, @Param("receiverId") String receiverId);

    @Query("MATCH (sender:User {id: $fromUserId}), (receiver:User {id: $toUserId}) MERGE (sender)-[:FRIEND_REQUEST]->(receiver)")
    void createFriendRequest(@Param("fromUserId") String fromUserId, @Param("toUserId") String toUserId);

    @Query("MATCH (u1:User {id: $userId})-[r:CONNECTED_TO]-(u2:User {id: $friendId}) DELETE r")
    void removeFriend(@Param("userId") String userId, @Param("friendId") String friendId);

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

    /**
     * Répare les utilisateurs qui n'ont pas de propriété 'id'.
     * Utilise l'ID interne ou génère un UUID si possible via Cypher.
     * Note: Neo4j 4.x+ supporte randomUUID()
     */
    @Query("MATCH (u:User) WHERE u.id IS NULL SET u.id = apoc.create.uuid() RETURN count(u)")
    int repairMissingIdsWithApoc();

    @Query("MATCH (u:User) WHERE u.id IS NULL AND u.email = $email SET u.id = $newId")
    void setUserIdByEmail(@Param("email") String email, @Param("newId") String newId);
}