package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SearchController {
    private final UserRepository userRepository;

    public SearchController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Page de recherche
    @GetMapping("/search")
    public String searchPage(
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (q != null && !q.trim().isEmpty()) {
            String currentUserId = userDetails.getUsername();
            List<User> results = searchUsers(q, currentUserId);
            model.addAttribute("query", q);
            model.addAttribute("results", results);
            model.addAttribute("resultsCount", results.size());
        }

        return "search";
    }

    // API de recherche (pour les appels AJAX)
    @GetMapping("/api/search")
    @ResponseBody
    public List<User> searchUsersApi(
            @RequestParam String q,
            @AuthenticationPrincipal UserDetails userDetails) {
        String currentUserId = userDetails.getUsername();
        return searchUsers(q, currentUserId);
    }

    private List<User> searchUsers(String query, String currentUserId) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String lowerQuery = query.toLowerCase().trim();

        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .filter(user -> {
                    String name = user.getName() != null ? user.getName().toLowerCase() : "";
                    String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                    String bio = user.getBio() != null ? user.getBio().toLowerCase() : "";

                    return name.contains(lowerQuery) ||
                            email.contains(lowerQuery) ||
                            bio.contains(lowerQuery);
                })
                .limit(20)
                .collect(Collectors.toList());
    }
}