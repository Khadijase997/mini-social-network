package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.dto.UserResponse;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Page d'accueil après connexion
    @GetMapping("/home")
    public String home(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        UserResponse userResponse = userService.toUserResponse(currentUser);
        model.addAttribute("user", userResponse);

        return "home";
    }

    // Voir son propre profil
    @GetMapping("/profile")
    public String viewOwnProfile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        UserResponse userResponse = userService.toUserResponse(currentUser);
        model.addAttribute("user", userResponse);
        model.addAttribute("isOwnProfile", true);

        return "profile";
    }

    // Voir le profil d'un autre utilisateur
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
        UserResponse userResponse = userService.toUserResponse(user);

        model.addAttribute("user", userResponse);
        model.addAttribute("isOwnProfile", currentUser.getId().equals(userId));

        return "profile";
    }

    // Page de modification du profil
    @GetMapping("/profile/edit")
    public String showEditProfilePage(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "edit-profile";
    }

    // Traitement de la modification du profil
    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam String name,
                                @RequestParam(required = false) String bio,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Mettre à jour les informations
        currentUser.setName(name);
        currentUser.setBio(bio);

        userService.updateUser(currentUser);

        redirectAttributes.addFlashAttribute("successMessage",
                "Profil mis à jour avec succès !");

        return "redirect:/profile";
    }

    // Méthode helper pour récupérer l'utilisateur connecté
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String email = authentication.getName();
        return userService.findByEmail(email).orElse(null);
    }
}