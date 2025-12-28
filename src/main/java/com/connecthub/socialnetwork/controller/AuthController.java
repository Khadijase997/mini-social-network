package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.dto.RegisterRequest;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Page d'accueil (redirection vers login)
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    // Afficher le formulaire d'inscription
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    // Traiter l'inscription
    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        // Vérifier que les mots de passe correspondent
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas");
            return "register";
        }

        // Vérifier la longueur du mot de passe
        if (request.getPassword().length() < 6) {
            model.addAttribute("error", "Le mot de passe doit contenir au moins 6 caractères");
            return "register";
        }

        try {
            // Créer l'utilisateur
            User user = userService.register(request);

            // Rediriger vers login avec message de succès
            redirectAttributes.addFlashAttribute("successMessage",
                    "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // Afficher le formulaire de connexion
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login";
    }
    // Déconnexion manuelle
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout=true";
    }
}
