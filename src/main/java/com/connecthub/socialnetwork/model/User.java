package com.connecthub.socialnetwork.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
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
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    private String name;
    private String email;
    private String password;
    private String bio;
    private LocalDateTime createdAt;
    private String profileImage;
    
    // Intérêts de l'utilisateur (pour recommandations et recherche)
    private Set<String> interests = new HashSet<>();
    
    // Liens externes (visibles uniquement pour les amis)
    private String whatsappLink;
    private String instagramLink;
    private String messengerLink;

    // Amis (relation bidirectionnelle)
    @Relationship(type = "CONNECTED_TO")
    private Set<User> friends = new HashSet<>();

    // Demandes d'amis envoyées
    @Relationship(type = "FRIEND_REQUEST", direction = Relationship.Direction.OUTGOING)
    private Set<User> sentFriendRequests = new HashSet<>();

    // Demandes d'amis reçues
    @Relationship(type = "FRIEND_REQUEST", direction = Relationship.Direction.INCOMING)
    private Set<User> receivedFriendRequests = new HashSet<>();
    
    // Utilisateurs bloqués
    @Relationship(type = "BLOCKED", direction = Relationship.Direction.OUTGOING)
    private Set<User> blockedUsers = new HashSet<>();
    
    // Publications créées
    @Relationship(type = "POSTED", direction = Relationship.Direction.OUTGOING)
    private Set<Post> posts = new HashSet<>();
    
    // Publications likées
    @Relationship(type = "LIKED_BY", direction = Relationship.Direction.OUTGOING)
    private Set<Post> likedPosts = new HashSet<>();
    
    // Commentaires créés
    @Relationship(type = "COMMENTED", direction = Relationship.Direction.OUTGOING)
    private Set<Comment> comments = new HashSet<>();


    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }


}
