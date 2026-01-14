package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
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

        fromUser.getSentFriendRequests().add(toUser);
        userRepository.save(fromUser);
    }

    public void acceptFriendRequest(String fromUserId, String toUserId) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur source introuvable"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur cible introuvable"));

        if (!toUser.getReceivedFriendRequests().contains(fromUser)) {
            throw new RuntimeException("Aucune demande trouvée");
        }

        toUser.getReceivedFriendRequests().remove(fromUser);
        fromUser.getSentFriendRequests().remove(toUser);

        fromUser.getFriends().add(toUser);
        toUser.getFriends().add(fromUser);

        userRepository.save(fromUser);
        userRepository.save(toUser);
    }

    public void rejectFriendRequest(String fromUserId, String toUserId) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur source introuvable"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur cible introuvable"));

        toUser.getReceivedFriendRequests().remove(fromUser);
        fromUser.getSentFriendRequests().remove(toUser);

        userRepository.save(fromUser);
        userRepository.save(toUser);
    }

    public void removeFriend(String userId, String friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Ami introuvable"));

        user.getFriends().remove(friend);
        friend.getFriends().remove(user);

        userRepository.save(user);
        userRepository.save(friend);
    }

    public List<User> getFriends(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return new ArrayList<>(user.getFriends());
    }

    public List<User> getReceivedFriendRequests(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return new ArrayList<>(user.getReceivedFriendRequests());
    }

    public List<User> getSentFriendRequests(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return new ArrayList<>(user.getSentFriendRequests());
    }

    /**
     * Retourne le statut de relation entre l'utilisateur courant et un autre utilisateur.
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
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Set<User> currentFriends = currentUser.getFriends();
        Set<User> sentRequests = currentUser.getSentFriendRequests();
        Set<User> receivedRequests = currentUser.getReceivedFriendRequests();

        // Map pour stocker les scores de recommandation
        Map<User, Integer> recommendationScores = new HashMap<>();

        // 1. Amis d'amis (score le plus élevé)
        for (User friend : currentFriends) {
            for (User friendOfFriend : friend.getFriends()) {
                if (!friendOfFriend.getId().equals(userId) &&
                        !currentFriends.contains(friendOfFriend) &&
                        !sentRequests.contains(friendOfFriend) &&
                        !receivedRequests.contains(friendOfFriend)) {

                    recommendationScores.put(friendOfFriend,
                            recommendationScores.getOrDefault(friendOfFriend, 0) + 10);
                }
            }
        }

        // 2. Amis communs (bonus supplémentaire)
        for (Map.Entry<User, Integer> entry : recommendationScores.entrySet()) {
            User candidate = entry.getKey();
            int mutualFriends = countMutualFriends(currentUser, candidate);
            entry.setValue(entry.getValue() + mutualFriends * 5);
        }

        // 3. Utilisateurs populaires (bonus mineur)
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (!user.getId().equals(userId) &&
                    !currentFriends.contains(user) &&
                    !sentRequests.contains(user) &&
                    !receivedRequests.contains(user) &&
                    !recommendationScores.containsKey(user)) {

                int popularity = user.getFriends().size();
                if (popularity > 5) {
                    recommendationScores.put(user, popularity / 2);
                }
            }
        }

        // Trier par score décroissant et retourner les top recommendations
        return recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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
     * Amélioration de l'algorithme de recommandation avec intérêts partagés
     */
    public List<User> getFriendRecommendationsWithInterests(String userId, int limit) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Set<User> currentFriends = currentUser.getFriends();
        Set<User> sentRequests = currentUser.getSentFriendRequests();
        Set<User> receivedRequests = currentUser.getReceivedFriendRequests();
        Set<User> blockedUsers = currentUser.getBlockedUsers();

        // Map pour stocker les scores de recommandation
        Map<User, Integer> recommendationScores = new HashMap<>();

        // 1. Amis d'amis (score le plus élevé: +10)
        for (User friend : currentFriends) {
            for (User friendOfFriend : friend.getFriends()) {
                if (!friendOfFriend.getId().equals(userId) &&
                        !currentFriends.contains(friendOfFriend) &&
                        !sentRequests.contains(friendOfFriend) &&
                        !receivedRequests.contains(friendOfFriend) &&
                        !blockedUsers.contains(friendOfFriend)) {

                    recommendationScores.put(friendOfFriend,
                            recommendationScores.getOrDefault(friendOfFriend, 0) + 10);
                }
            }
        }

        // 2. Amis communs (bonus: +5 par ami commun)
        for (Map.Entry<User, Integer> entry : recommendationScores.entrySet()) {
            User candidate = entry.getKey();
            int mutualFriends = countMutualFriends(currentUser, candidate);
            entry.setValue(entry.getValue() + mutualFriends * 5);
        }

        // 3. Intérêts partagés (bonus: +3 par intérêt commun)
        for (Map.Entry<User, Integer> entry : recommendationScores.entrySet()) {
            User candidate = entry.getKey();
            if (candidate.getInterests() != null && currentUser.getInterests() != null) {
                long commonInterests = currentUser.getInterests().stream()
                        .filter(candidate.getInterests()::contains)
                        .count();
                entry.setValue(entry.getValue() + (int) commonInterests * 3);
            }
        }

        // 4. Utilisateurs avec intérêts partagés (même s'ils ne sont pas amis d'amis)
        List<User> usersByInterests = userRepository.findUsersBySharedInterests(userId, 20);
        for (User candidate : usersByInterests) {
            if (!candidate.getId().equals(userId) &&
                    !currentFriends.contains(candidate) &&
                    !sentRequests.contains(candidate) &&
                    !receivedRequests.contains(candidate) &&
                    !blockedUsers.contains(candidate) &&
                    !recommendationScores.containsKey(candidate)) {

                // Score basé sur le nombre d'intérêts communs
                if (candidate.getInterests() != null && currentUser.getInterests() != null) {
                    long commonInterests = currentUser.getInterests().stream()
                            .filter(candidate.getInterests()::contains)
                            .count();
                    if (commonInterests > 0) {
                        recommendationScores.put(candidate, (int) commonInterests * 3);
                    }
                }
            }
        }

        // 5. Utilisateurs populaires (bonus mineur: +1 par 2 amis au-dessus de 5)
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (!user.getId().equals(userId) &&
                    !currentFriends.contains(user) &&
                    !sentRequests.contains(user) &&
                    !receivedRequests.contains(user) &&
                    !blockedUsers.contains(user) &&
                    !recommendationScores.containsKey(user)) {

                int popularity = user.getFriends().size();
                if (popularity > 5) {
                    recommendationScores.put(user, popularity / 2);
                }
            }
        }

        // Trier par score décroissant et retourner les top recommendations
        return recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}