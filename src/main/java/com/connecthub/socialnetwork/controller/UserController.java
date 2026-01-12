package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.dto.UserResponse;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.PostService;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;
    private final PostService postService;

    // ✅ UN SEUL CONSTRUCTEUR
    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    // =========================
    // PROFIL PERSONNEL
    // =========================
    @GetMapping("/profile")
    public String viewOwnProfile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", userService.toUserResponse(currentUser));
        model.addAttribute("isOwnProfile", true);

        // ✅ IMPORTANT : myPosts
        model.addAttribute("myPosts", postService.getFeedPosts(currentUser));

        return "profile";
    }

    // =========================
    // PROFIL AUTRE UTILISATEUR
    // =========================
    @GetMapping("/profile/{userId}")
    public String viewUserProfile(@PathVariable String userId, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/home";
        }

        User user = userOpt.get();

        model.addAttribute("user", userService.toUserResponse(user));
        model.addAttribute("isOwnProfile", currentUser.getId().equals(userId));

        // Posts de cet utilisateur
        model.addAttribute("myPosts", postService.getFeedPosts(user));

        return "profile";
    }

    // =========================
    // RÉCUP USER CONNECTÉ
    // =========================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        return userService.findByEmail(authentication.getName()).orElse(null);
    }
}