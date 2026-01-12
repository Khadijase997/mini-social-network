package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.model.Post;
import com.connecthub.socialnetwork.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedService {

    private final PostRepository postRepository;

    public FeedService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getFeed(String username) {
        return postRepository.findFeedPosts(username);
    }
}