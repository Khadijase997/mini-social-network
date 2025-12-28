package com.connecthub.socialnetwork.repository;

import com.connecthub.socialnetwork.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    Optional<User> findByEmail(String email);

    @Query("MATCH (u:User) WHERE toLower(u.name) CONTAINS toLower($name) RETURN u LIMIT 20")
    List<User> searchByName(String name);

    @Query("MATCH (u:User {id: $userId})-[:FRIENDS_WITH]-(friend) RETURN COUNT(friend)")
    int countFriends(String userId);
}