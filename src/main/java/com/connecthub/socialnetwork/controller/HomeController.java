package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.model.Post;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.FeedService;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    private final FeedService feedService;
    private final UserService userService;

    public HomeController(FeedService feedService, UserService userService) {
        this.feedService = feedService;
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String email = principal.getName(); // email de l'utilisateur connecté
            User currentUser = userService.getUserByEmail(email);
            List<Post> feed = feedService.getFeed(email);

            model.addAttribute("posts", feed != null ? feed : List.of());
            model.addAttribute("username", email);
            model.addAttribute("currentUser", currentUser);
        } catch (Exception e) {
            // En cas d'erreur, initialiser avec des listes vides
            model.addAttribute("posts", List.of());
            model.addAttribute("error", "Erreur lors du chargement du feed: " + e.getMessage());
        }

        return "home";
    }
}