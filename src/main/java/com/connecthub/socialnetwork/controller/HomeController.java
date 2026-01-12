package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.model.Post;
import com.connecthub.socialnetwork.service.FeedService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    private final FeedService feedService;

    public HomeController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {

        String username = principal.getName(); // utilisateur connecté
        List<Post> feed = feedService.getFeed(username);

        model.addAttribute("posts", feed);
        model.addAttribute("username", username);

        return "home";
    }
}