package com.connecthub.socialnetwork.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Node("User")
public class User {

    @Id
    @GeneratedValue
    private String id;

    private String name;
    private String email;
    private String password;
    private String bio;
    private LocalDateTime createdAt;
    private String profileImage;

    // Amis (relation bidirectionnelle)
    @Relationship(type = "FRIENDS_WITH")
    private Set<User> friends = new HashSet<>();

    // Demandes d'amis envoyées
    @Relationship(type = "FRIEND_REQUEST", direction = Relationship.Direction.OUTGOING)
    private Set<User> sentFriendRequests = new HashSet<>();

    // Demandes d'amis reçues
    @Relationship(type = "FRIEND_REQUEST", direction = Relationship.Direction.INCOMING)
    private Set<User> receivedFriendRequests = new HashSet<>();


    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }


}
