package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.dto.RegisterRequest;
import com.connecthub.socialnetwork.dto.UserResponse;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Service pour la gestion des utilisateurs
 * Gère l'inscription, modification de profil, changement de mot de passe et statistiques
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Créer l'utilisateur avec mot de passe hashé
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())  // 🔒 HASHER avec BCrypt
        );

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    // NOUVELLE MÉTHODE - Retourne directement l'utilisateur (pas Optional)
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + userId));
    }

    // NOUVELLE MÉTHODE - Retourne directement l'utilisateur par email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'email: " + email));
    }


    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImage()

        );

        int friendsCount = userRepository.countFriends(user.getId());
        response.setFriendsCount(friendsCount);

        return response;
    }

    /**
     * Met à jour le profil d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param name Nouveau nom (optionnel)
     * @param bio Nouvelle bio (optionnel)
     * @param profileImage Nouvelle image de profil (optionnel)
     * @param interests Nouveaux intérêts (optionnel)
     * @param whatsappLink Lien WhatsApp (optionnel)
     * @param instagramLink Lien Instagram (optionnel)
     * @param messengerLink Lien Messenger (optionnel)
     * @return Utilisateur mis à jour
     */
    @Transactional
    public User updateProfile(String userId, String name, String bio, String profileImage,
                              Set<String> interests, String whatsappLink, String instagramLink, String messengerLink) {
        User user = getUserById(userId);

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (profileImage != null && !profileImage.trim().isEmpty()) {
            user.setProfileImage(profileImage);
        }
        if (interests != null) {
            user.setInterests(interests);
        }
        if (whatsappLink != null) {
            user.setWhatsappLink(whatsappLink);
        }
        if (instagramLink != null) {
            user.setInstagramLink(instagramLink);
        }
        if (messengerLink != null) {
            user.setMessengerLink(messengerLink);
        }

        return userRepository.save(user);
    }

    /**
     * Change le mot de passe d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param currentPassword Mot de passe actuel (pour vérification)
     * @param newPassword Nouveau mot de passe
     * @throws RuntimeException si le mot de passe actuel est incorrect
     */
    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        // Valider le nouveau mot de passe
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Le nouveau mot de passe doit contenir au moins 6 caractères");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Récupère les statistiques d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Objet contenant les statistiques
     */
    public UserStatistics getUserStatistics(String userId) {
        int friendsCount = userRepository.countFriends(userId);
        int postsCount = userRepository.countPosts(userId);
        int totalLikesReceived = userRepository.countTotalLikesReceived(userId);

        return new UserStatistics(friendsCount, postsCount, totalLikesReceived);
    }

    /**
     * Classe interne pour les statistiques utilisateur
     */
    public static class UserStatistics {
        private final int friendsCount;
        private final int postsCount;
        private final int totalLikesReceived;

        public UserStatistics(int friendsCount, int postsCount, int totalLikesReceived) {
            this.friendsCount = friendsCount;
            this.postsCount = postsCount;
            this.totalLikesReceived = totalLikesReceived;
        }

        public int getFriendsCount() {
            return friendsCount;
        }

        public int getPostsCount() {
            return postsCount;
        }

        public int getTotalLikesReceived() {
            return totalLikesReceived;
        }
    }
}
