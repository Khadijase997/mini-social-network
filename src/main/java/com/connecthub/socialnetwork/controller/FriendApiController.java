package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.dto.UserResponse;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.FriendService;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST API pour la gestion des amis
 * Endpoints: /api/friends/*
 */
@RestController
@RequestMapping("/api/friends")
public class FriendApiController {

    private final FriendService friendService;
    private final UserService userService;

    public FriendApiController(FriendService friendService, UserService userService) {
        this.friendService = friendService;
        this.userService = userService;
    }

    /**
     * Récupère l'utilisateur connecté en toute sécurité
     */
    private User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        User user = userService.getUserByEmail(userDetails.getUsername());

        if (user == null || user.getId() == null) {
            throw new RuntimeException("Utilisateur introuvable ou ID null");
        }

        return user;
    }

    // =========================
    // SECTION 1: Demandes reçues
    // =========================

    @GetMapping("/requests/received")
    public ResponseEntity<List<User>> getReceivedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);
        return ResponseEntity.ok(
                friendService.getReceivedFriendRequests(current.getId())
        );
    }

    @PostMapping("/accept/{fromUserId}")
    public ResponseEntity<Map<String, String>> acceptRequest(
            @PathVariable String fromUserId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);
        friendService.acceptFriendRequest(fromUserId, current.getId());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Demande d'ami acceptée"
        ));
    }

    @DeleteMapping("/reject/{fromUserId}")
    public ResponseEntity<Map<String, String>> rejectRequest(
            @PathVariable String fromUserId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);
        friendService.rejectFriendRequest(fromUserId, current.getId());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Demande d'ami refusée"
        ));
    }

    // =========================
    // SECTION 2: Demandes envoyées
    // =========================

    @GetMapping("/requests/sent")
    public ResponseEntity<List<User>> getSentRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);
        return ResponseEntity.ok(
                friendService.getSentFriendRequests(current.getId())
        );
    }

    @DeleteMapping("/cancel/{toUserId}")
    public ResponseEntity<Map<String, String>> cancelRequest(
            @PathVariable String toUserId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);
        friendService.rejectFriendRequest(current.getId(), toUserId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Demande d'ami annulée"
        ));
    }

    // =========================
    // SECTION 3: Liste des amis
    // =========================

    @GetMapping
    public ResponseEntity<List<User>> getFriends(
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);
        return ResponseEntity.ok(
                friendService.getFriends(current.getId())
        );
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Map<String, String>> removeFriend(
            @PathVariable String friendId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);
        friendService.removeFriend(current.getId(), friendId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Ami retiré avec succès"
        ));
    }

    // =========================
    // SECTION 4: Suggestions d'amis
    // =========================

    @GetMapping("/suggestions")
    public ResponseEntity<List<UserResponse>> getSuggestions(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);

        List<UserResponse> response = friendService
                .getFriendRecommendationsWithInterests(current.getId(), limit)
                .stream()
                .map(user -> {
                    UserResponse dto = userService.toUserResponse(user);
                    dto.setRelationStatus(friendService.getRelationStatus(current, user));
                    dto.setMutualFriendsCount(
                            friendService.getMutualFriendsCount(current.getId(), user.getId())
                    );
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    // =========================
    // SECTION 5: Envoyer une demande
    // =========================

    @PostMapping("/request/{toUserId}")
    public ResponseEntity<Map<String, String>> sendFriendRequest(
            @PathVariable String toUserId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User current = getCurrentUser(userDetails);
        friendService.sendFriendRequest(current.getId(), toUserId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Demande d'ami envoyée avec succès"
        ));
    }
}
