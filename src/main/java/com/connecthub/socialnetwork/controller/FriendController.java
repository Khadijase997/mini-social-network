package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.FriendService;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;
    private final UserService userService;

    public FriendController(FriendService friendService, UserService userService) {
        this.friendService = friendService;
        this.userService = userService;
    }

    // Envoyer une demande d'ami
    @PostMapping("/request/{toUserId}")
    @ResponseBody
    public ResponseEntity<?> sendFriendRequest(
            @PathVariable String toUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String fromUserId = currentUser.getId();

            friendService.sendFriendRequest(fromUserId, toUserId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Demande d'ami envoyée avec succès");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Accepter une demande d'ami
    @PostMapping("/accept/{fromUserId}")
    @ResponseBody
    public ResponseEntity<?> acceptFriendRequest(
            @PathVariable String fromUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String toUserId = currentUser.getId();

            friendService.acceptFriendRequest(fromUserId, toUserId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Demande d'ami acceptée");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Refuser une demande d'ami
    @PostMapping("/reject/{fromUserId}")
    @ResponseBody
    public ResponseEntity<?> rejectFriendRequest(
            @PathVariable String fromUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String toUserId = currentUser.getId();

            friendService.rejectFriendRequest(fromUserId, toUserId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Demande d'ami refusée");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Supprimer un ami
    @DeleteMapping("/remove/{friendId}")
    @ResponseBody
    public ResponseEntity<?> removeFriend(
            @PathVariable String friendId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String userId = currentUser.getId();

            friendService.removeFriend(userId, friendId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Ami supprimé avec succès");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Obtenir la liste des amis
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getFriendsList(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String userId = currentUser.getId();

            List<User> friends = friendService.getFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Obtenir les demandes reçues
    @GetMapping("/requests/received")
    @ResponseBody
    public ResponseEntity<?> getReceivedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String userId = currentUser.getId();

            List<User> requests = friendService.getReceivedFriendRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Obtenir les demandes envoyées
    @GetMapping("/requests/sent")
    @ResponseBody
    public ResponseEntity<?> getSentRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String userId = currentUser.getId();

            List<User> requests = friendService.getSentFriendRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Obtenir les recommandations d'amis
    @GetMapping("/recommendations")
    @ResponseBody
    public ResponseEntity<?> getRecommendations(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String userId = currentUser.getId();

            List<User> recommendations = friendService.getFriendRecommendations(userId, limit);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Obtenir le nombre d'amis en commun
    @GetMapping("/mutual/{userId}")
    @ResponseBody
    public ResponseEntity<?> getMutualFriendsCount(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email);
            String currentUserId = currentUser.getId();

            int count = friendService.getMutualFriendsCount(currentUserId, userId);
            return ResponseEntity.ok(Map.of("mutualFriendsCount", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}