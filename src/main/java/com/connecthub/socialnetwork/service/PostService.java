package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.model.Comment;
import com.connecthub.socialnetwork.model.Post;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.CommentRepository;
import com.connecthub.socialnetwork.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des publications
 * Gère les CRUD, likes, commentaires et feed
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Récupère le feed des amis de l'utilisateur
     */
    public List<Post> getFeedPosts(User user) {
        return postRepository.findFeedPosts(user.getName());
    }

    /**
     * Crée une nouvelle publication
     * 
     * @param author   Auteur de la publication
     * @param content  Contenu textuel
     * @param imageUrl URL de l'image (optionnel)
     * @return Publication créée
     */
    @Transactional
    public Post createPost(User author, String content, String imageUrl) {
        Post post = new Post();
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setCreatedAt(LocalDateTime.now());
        post.setAuthor(author);

        Post savedPost = postRepository.save(post);

        // Mettre à jour la relation POSTED dans l'auteur
        // Neo4j Spring Data gère automatiquement la relation bidirectionnelle
        // mais il faut sauvegarder l'auteur pour persister la relation
        author.getPosts().add(savedPost);
        // Note: La relation sera persistée automatiquement lors de la sauvegarde du
        // post
        // grâce à @Relationship dans le modèle Post

        return savedPost;
    }

    /**
     * Récupère une publication par son ID
     */
    public Optional<Post> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    /**
     * Supprime une publication (seulement par son auteur)
     * 
     * @param postId ID de la publication
     * @param userId ID de l'utilisateur (vérification de sécurité)
     * @throws RuntimeException si l'utilisateur n'est pas l'auteur
     */
    @Transactional
    public void deletePost(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publication introuvable"));

        if (post.getAuthor() == null) {
            throw new RuntimeException("Erreur d'intégrité : Auteur de la publication introuvable");
        }

        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette publication");
        }

        // Utilisation de la méthode Cypher pour suppression propre et complète (inc.
        // commentaires)
        postRepository.deletePostById(postId);
    }

    /**
     * Like/Unlike une publication
     * 
     * @param postId ID de la publication
     * @param user   Utilisateur qui like/unlike
     * @return true si liké, false si unliké
     */
    @Transactional
    public boolean toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publication introuvable"));

        boolean hasLiked = postRepository.hasUserLikedPost(user.getId(), postId);

        if (hasLiked) {
            // Unlike: retirer la relation LIKED_BY
            post.getLikes().remove(user);
            user.getLikedPosts().remove(post);
        } else {
            // Like: ajouter la relation LIKED_BY
            post.getLikes().add(user);
            user.getLikedPosts().add(post);
        }

        postRepository.save(post);
        return !hasLiked;
    }

    /**
     * Vérifie si un utilisateur a liké une publication
     */
    public boolean hasUserLiked(Long postId, String userId) {
        return postRepository.hasUserLikedPost(userId, postId);
    }

    /**
     * Compte le nombre de likes d'une publication
     */
    public int getLikeCount(Long postId) {
        return postRepository.countLikes(postId);
    }

    /**
     * Ajoute un commentaire à une publication
     * 
     * @param postId  ID de la publication
     * @param user    Auteur du commentaire
     * @param content Contenu du commentaire
     * @return Commentaire créé
     */
    @Transactional
    public Comment addComment(Long postId, User user, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publication introuvable"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setAuthor(user);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);

        // Mettre à jour la relation HAS_COMMENT
        post.getComments().add(savedComment);
        postRepository.save(post);

        // Mettre à jour la relation COMMENTED
        user.getComments().add(savedComment);

        return savedComment;
    }

    /**
     * Récupère tous les commentaires d'une publication
     */
    public List<Comment> getComments(Long postId) {
        return commentRepository.findCommentsByPostId(postId);
    }

    /**
     * Compte le nombre de commentaires d'une publication
     */
    public int getCommentCount(Long postId) {
        return postRepository.countComments(postId);
    }

    /**
     * Récupère les publications d'un utilisateur
     */
    public List<Post> getUserPosts(String userId) {
        return postRepository.findPostsByUserId(userId);
    }

    /**
     * Récupère la liste des utilisateurs qui ont liké une publication
     */
    public List<User> getLikers(Long postId) {
        return postRepository.findLikers(postId);
    }
}