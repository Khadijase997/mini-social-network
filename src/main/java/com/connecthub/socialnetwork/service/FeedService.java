package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.model.Post;
import com.connecthub.socialnetwork.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final PostRepository postRepository;

    public FeedService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<Post> getFeed(String email) {
        List<Post> posts = postRepository.findFeedPostsByEmail(email);
        
        // Si aucun post n'est trouvé, retourner une liste vide
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }
        
        // Charger les relations pour chaque post (author, likes, comments)
        // Neo4j Spring Data charge automatiquement les relations définies avec @Relationship
        // En récupérant chaque post individuellement, on force le chargement des relations
        return posts.stream()
                .map(post -> {
                    // Récupérer le post avec toutes ses relations
                    return postRepository.findById(post.getId())
                            .orElse(post);
                })
                .collect(Collectors.toList());
    }
}