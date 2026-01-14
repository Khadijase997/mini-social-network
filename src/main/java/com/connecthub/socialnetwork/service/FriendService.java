package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@org.springframework.transaction.annotation.Transactional
public class FriendService {
    private final UserRepository userRepository;

    public FriendService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void sendFriendRequest(String fromUserId, String toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("Impossible de s'envoyer une demande à soi-même");
        }
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur source introuvable"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur cible introuvable"));

        if (fromUser.getFriends().contains(toUser)) {
            throw new RuntimeException("Vous êtes déjà amis");
        }
        if (fromUser.getSentFriendRequests().contains(toUser)) {
            throw new RuntimeException("Demande déjà envoyée");
        }
        if (fromUser.getReceivedFriendRequests().contains(toUser)) {
            // If the other person already sent a request, just accept it?
            // For now, let's just throw or handle. The UI usually handles this state.
            // But technically we should probably auto-accept if mutual.
            // Sticking to simple logic for now as requested.
            throw new RuntimeException("Cet utilisateur vous a déjà envoyé une demande");
        }

        fromUser.getSentFriendRequests().add(toUser);
        userRepository.save(fromUser);
    }

    public void acceptFriendRequest(String fromUserId, String toUserId) {
        // Validation basique
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("Impossible d'accepter une demande de soi-même");
        }

        // Utilisation de la requête Cypher native pour une atomicité et fiabilité
        // garanties
        // fromUserId = Sender (celui qui a envoyé la demande)
        // toUserId = Receiver (celui qui accepte)
        userRepository.acceptFriendRequest(fromUserId, toUserId);
    }

    public void rejectFriendRequest(String fromUserId, String toUserId) {
        // Utilisation de la requête Cypher native pour fiabilité
        userRepository.rejectFriendRequest(fromUserId, toUserId);
    }

    public void removeFriend(String userId, String friendId) {
        // Utilisation de la requête Cypher native pour fiabilité
        userRepository.removeFriend(userId, friendId);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<User> getFriends(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        // Force initialization to avoid LazyInitializationException if outside
        // transaction
        List<User> friends = new ArrayList<>(user.getFriends());
        return friends;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<User> getReceivedFriendRequests(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return new ArrayList<>(user.getReceivedFriendRequests());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<User> getSentFriendRequests(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return new ArrayList<>(user.getSentFriendRequests());
    }

    /**
     * Retourne le statut de relation entre l'utilisateur courant et un autre
     * utilisateur.
     * FRIEND, REQUEST_SENT, REQUEST_RECEIVED ou NONE.
     */
    public String getRelationStatus(User currentUser, User other) {
        if (currentUser.getFriends().contains(other)) {
            return "FRIEND";
        }
        if (currentUser.getSentFriendRequests().contains(other)) {
            return "REQUEST_SENT";
        }
        if (currentUser.getReceivedFriendRequests().contains(other)) {
            return "REQUEST_RECEIVED";
        }
        return "NONE";
    }

    // Algorithme de recommandation d'amis
    public List<User> getFriendRecommendations(String userId, int limit) {
        return getFriendRecommendationsWithInterests(userId, limit);
    }

    private int countMutualFriends(User user1, User user2) {
        Set<User> friends1 = user1.getFriends();
        Set<User> friends2 = user2.getFriends();

        return (int) friends1.stream()
                .filter(friends2::contains)
                .count();
    }

    public int getMutualFriendsCount(String userId1, String userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return countMutualFriends(user1, user2);
    }

    /**
     * Bloque un utilisateur
     * Supprime automatiquement l'amitié et les demandes en attente
     */
    public void blockUser(String userId, String blockedUserId) {
        if (userId.equals(blockedUserId)) {
            throw new RuntimeException("Impossible de se bloquer soi-même");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur à bloquer introuvable"));

        // Retirer l'amitié si elle existe
        user.getFriends().remove(blockedUser);
        blockedUser.getFriends().remove(user);

        // Retirer les demandes d'amitié
        user.getSentFriendRequests().remove(blockedUser);
        user.getReceivedFriendRequests().remove(blockedUser);
        blockedUser.getSentFriendRequests().remove(user);
        blockedUser.getReceivedFriendRequests().remove(user);

        // Ajouter le blocage
        user.getBlockedUsers().add(blockedUser);

        userRepository.save(user);
        userRepository.save(blockedUser);
    }

    /**
     * Débloque un utilisateur
     */
    public void unblockUser(String userId, String unblockedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        User unblockedUser = userRepository.findById(unblockedUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.getBlockedUsers().remove(unblockedUser);
        userRepository.save(user);
    }

    /**
     * Vérifie si un utilisateur est bloqué
     */
    public boolean isBlocked(String userId, String otherUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return user.getBlockedUsers().stream()
                .anyMatch(blocked -> blocked.getId().equals(otherUserId));
    }

    /**
     * Recommandation basée STRICTEMENT sur les centres d'intérêt communs.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<User> getFriendRecommendationsWithInterests(String userId, int limit) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Set<String> myInterests = currentUser.getInterests();
        if (myInterests == null || myInterests.isEmpty()) {
            return new ArrayList<>(); // Pas d'intérêts, pas de reco basées sur intérêts
        }

        // Cache des IDs à exclure (Soi-même, Amis, Demandes reçues, Demandes envoyées,
        // Bloqués)
        Set<String> excludedIds = new HashSet<>();
        excludedIds.add(userId);
        currentUser.getFriends().forEach(u -> excludedIds.add(u.getId()));
        currentUser.getSentFriendRequests().forEach(u -> excludedIds.add(u.getId()));
        currentUser.getReceivedFriendRequests().forEach(u -> excludedIds.add(u.getId()));
        currentUser.getBlockedUsers().forEach(u -> excludedIds.add(u.getId()));

        // Récupérer les candidats potentiels
        // L'idéal serait une requête Cypher optimisée, mais pour rester simple et
        // éviter StackOverflow:
        // On cherche les utilisateurs qui ont au moins UN intérêt en commun.
        List<User> candidates = userRepository.findUsersBySharedInterests(userId, 50); // Fetch top 50 matches

        // Calcul du score et filtrage final
        Map<User, Integer> scoredCandidates = new HashMap<>();

        for (User candidate : candidates) {
            String cid = candidate.getId();
            if (excludedIds.contains(cid)) {
                continue;
            }

            Set<String> candidateInterests = candidate.getInterests();
            if (candidateInterests == null)
                continue;

            // Compter les intérêts communs (Intersection)
            long commonCount = myInterests.stream()
                    .filter(candidateInterests::contains)
                    .count();

            if (commonCount > 0) {
                // Score = nombre d'intérêts communs * 10
                scoredCandidates.put(candidate, (int) commonCount * 10);
            }
        }

        // Trier par score décroissant
        return scoredCandidates.entrySet().stream()
                .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}