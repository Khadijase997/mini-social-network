package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.dto.UserResponse;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.FriendService;
import com.connecthub.socialnetwork.service.PostService;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Contrôleur pour la gestion des profils utilisateur
 * Gère l'affichage, modification et statistiques
 */
@Controller
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final FriendService friendService;

    public UserController(UserService userService, PostService postService, FriendService friendService) {
        this.userService = userService;
        this.postService = postService;
        this.friendService = friendService;
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

        model.addAttribute("user", currentUser);
        model.addAttribute("isOwnProfile", true);

        // Publications de l'utilisateur
        model.addAttribute("myPosts", postService.getUserPosts(currentUser.getId()));

        // Statistiques
        UserService.UserStatistics stats = userService.getUserStatistics(currentUser.getId());
        model.addAttribute("stats", stats);

        // Vérifier si l'utilisateur a des amis pour afficher les liens externes
        boolean hasFriends = friendService.getFriends(currentUser.getId()).size() > 0;
        model.addAttribute("hasFriends", hasFriends);

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

        // Vérifier si l'utilisateur est bloqué
        if (friendService.isBlocked(currentUser.getId(), userId) ||
                friendService.isBlocked(userId, currentUser.getId())) {
            return "redirect:/home";
        }

        model.addAttribute("user", user);
        model.addAttribute("isOwnProfile", currentUser.getId().equals(userId));

        // Posts de cet utilisateur
        model.addAttribute("myPosts", postService.getUserPosts(userId));

        // Statistiques
        UserService.UserStatistics stats = userService.getUserStatistics(userId);
        model.addAttribute("stats", stats);

        // Vérifier si les utilisateurs sont amis (pour afficher les liens externes)
        boolean areFriends = friendService.getFriends(currentUser.getId()).stream()
                .anyMatch(friend -> friend.getId().equals(userId));
        model.addAttribute("areFriends", areFriends);

        // Amis en commun
        int mutualFriends = friendService.getMutualFriendsCount(currentUser.getId(), userId);
        model.addAttribute("mutualFriends", mutualFriends);

        return "profile";
    }

    private static final List<String> ALL_INTERESTS = java.util.stream.Stream.of(
            "Art", "Beauty", "Books", "Business and entrepreneurship", "Cars and automobiles",
            "Cooking", "DIY and crafts", "Education and learning", "Fashion", "Finance and investments",
            "Fitness", "Food and dining", "Gaming", "Gardening", "Health and wellness",
            "History", "Movies", "Music", "Nature", "Outdoor activities",
            "Parenting and family", "Pets", "Photography", "Politics", "Science",
            "Social causes and activism", "Sports", "Technology", "Travel").sorted()
            .collect(java.util.stream.Collectors.toList());

    // =========================
    // MODIFICATION DE PROFIL
    // =========================
    @GetMapping("/profile/edit")
    public String editProfilePage(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("allInterests", ALL_INTERESTS);
        return "edit-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String profileImage,
            @RequestParam(required = false) List<String> interests,
            @RequestParam(required = false) String whatsappLink,
            @RequestParam(required = false) String instagramLink,
            @RequestParam(required = false) String messengerLink,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Parser les intérêts
            Set<String> interestsSet = new HashSet<>();
            if (interests != null) {
                interestsSet.addAll(interests);
            }

            userService.updateProfile(
                    currentUser.getId(),
                    name,
                    bio,
                    profileImage,
                    interestsSet,
                    whatsappLink,
                    instagramLink,
                    messengerLink);

            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // =========================
    // CHANGEMENT DE MOT DE PASSE
    // =========================
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas");
                return "redirect:/profile/edit";
            }

            userService.changePassword(currentUser.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/profile/edit";
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

        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user != null) {
            System.out.println("DEBUG: Current User loaded: " + user.getEmail() + " with ID: " + user.getId());
        } else {
            System.out.println("DEBUG: No current user found for email: " + authentication.getName());
        }
        return user;
    }
}