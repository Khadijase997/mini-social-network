package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.model.Comment;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.PostService;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contrôleur pour la gestion des publications
 * Gère la création, suppression, likes et commentaires
 */
@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;
    
    // Dossier de stockage des images (dans le classpath)
    private static final String UPLOAD_DIR = "src/main/resources/static/images/posts/";
    private static final String STATIC_DIR = "static/images/posts/";

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
        
        // Créer le dossier s'il n'existe pas
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du dossier d'upload: " + e.getMessage());
        }
    }

    /**
     * Crée une nouvelle publication avec image optionnelle
     */
    @PostMapping("/posts")
    public String createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User currentUser = userService.getUserByEmail(email);

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = saveImage(image);
            } catch (IOException e) {
                // En cas d'erreur, on continue sans l'image
                System.err.println("Erreur lors de l'upload de l'image: " + e.getMessage());
            }
        }

        postService.createPost(currentUser, content, imageUrl);

        return "redirect:/home";
    }

    /**
     * Supprime une publication (seulement par son auteur)
     */
    @DeleteMapping("/posts/{postId}")
    @ResponseBody
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);

            postService.deletePost(postId, currentUser.getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Publication supprimée avec succès");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Like/Unlike une publication (endpoint AJAX)
     */
    @PostMapping("/posts/{postId}/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);

            boolean isLiked = postService.toggleLike(postId, currentUser);
            int likeCount = postService.getLikeCount(postId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", isLiked);
            response.put("likeCount", likeCount);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Vérifie si l'utilisateur a liké une publication
     */
    @GetMapping("/posts/{postId}/like-status")
    @ResponseBody
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);

            boolean hasLiked = postService.hasUserLiked(postId, currentUser.getId());
            int likeCount = postService.getLikeCount(postId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", hasLiked);
            response.put("likeCount", likeCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Ajoute un commentaire à une publication
     */
    @PostMapping("/posts/{postId}/comments")
    @ResponseBody
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestParam("content") String content,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);

            Comment comment = postService.addComment(postId, currentUser, content);
            int commentCount = postService.getCommentCount(postId);

            Map<String, Object> response = new HashMap<>();
            response.put("comment", Map.of(
                    "id", comment.getId(),
                    "content", comment.getContent(),
                    "authorName", comment.getAuthor().getName(),
                    "createdAt", comment.getCreatedAt()
            ));
            response.put("commentCount", commentCount);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les commentaires d'une publication
     */
    @GetMapping("/posts/{postId}/comments")
    @ResponseBody
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        try {
            List<Comment> comments = postService.getComments(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Sauvegarde une image uploadée et retourne son URL
     */
    private String saveImage(MultipartFile image) throws IOException {
        // Générer un nom de fichier unique
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // Sauvegarder le fichier
        Path filePath = Paths.get(UPLOAD_DIR + filename);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner l'URL relative
        return "/images/posts/" + filename;
    }
}