package com.jkotima.seinahullu.controllers;

import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import com.jkotima.seinahullu.models.Post;
import com.jkotima.seinahullu.models.User;
import com.jkotima.seinahullu.payload.request.PostRequest;
import com.jkotima.seinahullu.repository.PostRepository;
import com.jkotima.seinahullu.repository.UserRepository;
import com.jkotima.seinahullu.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
public class PostController {
  @Autowired
  PostRepository postRepository;

  @Autowired
  UserRepository userRepository;

  @PostMapping("/")
  Post createPost(@Valid @RequestBody PostRequest req) {
    UserDetailsImpl contextUser =
        (UserDetailsImpl) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

    Post post =
        new Post(
            new Date(),
            req.getContent(),
            userRepository.findById(contextUser.getId()).get());

    return postRepository.save(post);
  }

  @GetMapping("/")
  public List<Post> getPostsFromFollowedUsers(Authentication authentication) {
    User currentUser = userRepository.findByUsername(authentication.getName()).get();
    List<User> follows = currentUser.getFollows();

    return postRepository.findByUserIn(follows).get();
  }
  
  @GetMapping("/{id}")
  public Post getPost(@PathVariable long id) {
    return postRepository.findById(id).get();
  }
  
}
