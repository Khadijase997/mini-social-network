package com.connecthub.socialnetwork.controller;

import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.service.PostService;
import com.connecthub.socialnetwork.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @PostMapping("/posts")
    public String createPost(@RequestParam("content") String content) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User currentUser = userService.getUserByEmail(email);

        postService.createPost(currentUser, content);

        return "redirect:/home";
    }
}