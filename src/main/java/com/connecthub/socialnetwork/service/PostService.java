package com.connecthub.socialnetwork.service;

import com.connecthub.socialnetwork.model.Post;
import com.connecthub.socialnetwork.model.User;
import com.connecthub.socialnetwork.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Feed des amis
    public List<Post> getFeedPosts(User user) {
        return postRepository.findFeedPosts(user.getName());
    }

    // Créer un post
    public Post createPost(User author, String content) {
        Post post = new Post();
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());
        post.setAuthor(author);

        return postRepository.save(post);
    }
}