package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.dto.UserResponse;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.UserRepository;
import com.connecthub.socialnetwork.service.FriendService;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class SearchController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final FriendService friendService;

    public SearchController(UserRepository userRepository,
                            UserService userService,
                            FriendService friendService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.friendService = friendService;
    }

    /* =========================
       PAGE /search
       ========================= */
    @GetMapping("/search")
    public String searchPage(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String filter,
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        User currentUser = userService.getUserByEmail(principal.getName());

        List<User> results;

        if ("interests".equals(filter)) {
            results = userRepository.findUsersBySharedInterests(currentUser.getId(), 20);
        } else if (q != null && !q.trim().isEmpty()) {
            results = userRepository.searchUsers(q.trim());
        } else {
            results = userRepository.findSomeUsers(50);
        }

        // Filtrage sécurisé
        results.removeIf(user -> shouldExcludeUser(user, currentUser));

        if (results.size() > 50) {
            results = results.subList(0, 50);
        }

        model.addAttribute("query", q);
        model.addAttribute("filter", filter);
        model.addAttribute("results", results);
        model.addAttribute("resultsCount", results.size());

        return "search";
    }

    /* =========================
       API /api/search (AJAX)
       ========================= */
    @GetMapping("/api/search")
    @ResponseBody
    public List<User> searchUsersApi(
            @RequestParam String q,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        List<User> results = userRepository.searchUsers(q);

        results.removeIf(user -> shouldExcludeUser(user, currentUser));

        return results;
    }

    /* =========================
       API /api/users/search
       ========================= */
    @GetMapping("/api/users/search")
    @ResponseBody
    public List<UserResponse> searchUsersWithStatus(
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByEmail(userDetails.getUsername());

        List<User> users = (q != null && !q.trim().isEmpty())
                ? userRepository.searchUsers(q.trim())
                : userRepository.findSomeUsers(50);

        users.removeIf(user -> shouldExcludeUser(user, currentUser));

        return users.stream().map(user -> {
            UserResponse dto = userService.toUserResponse(user);
            dto.setRelationStatus(friendService.getRelationStatus(currentUser, user));
            dto.setMutualFriendsCount(
                    friendService.getMutualFriendsCount(
                            currentUser.getId(),
                            user.getId()
                    )
            );
            return dto;
        }).toList();
    }

    /* =========================
       MÉTHODE UTILITAIRE SAFE
       ========================= */
    private boolean shouldExcludeUser(User user, User currentUser) {

        if (user == null || user.getId() == null) return true;
        if (currentUser == null || currentUser.getId() == null) return true;

        // Ne pas afficher moi-même
        if (currentUser.getId().equals(user.getId())) return true;

        // Utilisateurs bloqués
        if (currentUser.getBlockedUsers() != null) {
            return currentUser.getBlockedUsers().stream()
                    .anyMatch(blocked ->
                            blocked != null &&
                                    blocked.getId() != null &&
                                    blocked.getId().equals(user.getId())
                    );
        }

        return false;
    }
}
