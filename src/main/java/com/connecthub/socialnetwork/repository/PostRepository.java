package com.connecthub.socialnetwork.repository;

import com.connecthub.socialnetwork.model.Post;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends Neo4jRepository<Post, Long> {

    // 🔹 Feed des amis
    @Query("""
        MATCH (me:User {name: $username})-[:FRIENDS_WITH]->(f:User)
        MATCH (f)-[:POSTED]->(p:Post)
        RETURN p
        ORDER BY p.createdAt DESC
    """)
    List<Post> findFeedPosts(@Param("username") String username);

    // 🔹 Mes publications (IMPORTANT)
    @Query("""
        MATCH (u:User {email: $email})-[:POSTED]->(p:Post)
        RETURN p
        ORDER BY p.createdAt DESC
    """)
    List<Post> findPostsByUserEmail(@Param("email") String email);
}