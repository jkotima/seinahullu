package com.jkotima.seinahullu.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Date;
import com.jayway.jsonpath.JsonPath;
import com.jkotima.seinahullu.models.ERole;
import com.jkotima.seinahullu.models.Post;
import com.jkotima.seinahullu.models.User;
import com.jkotima.seinahullu.repository.PostRepository;
import com.jkotima.seinahullu.repository.RoleRepository;
import com.jkotima.seinahullu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class PostControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  PasswordEncoder encoder;

  @BeforeEach
  public void init() {
    userRepository.deleteAll();

    String encodedPw = encoder.encode("password123!");

    User normalUser = new User("normalUser", "normalUser@test.com", encodedPw);
    normalUser.getRoles().add(roleRepository.findByName(ERole.ROLE_USER).get());

    userRepository.save(normalUser);
  }

  private String accessTokenFor(String username, String password) throws Exception {
    MvcResult res = mockMvc.perform(post("/api/auth/signin")
        .contentType(MediaType.APPLICATION_JSON)
        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password)))
        .andReturn();

    return JsonPath.read(res.getResponse().getContentAsString(), "$.accessToken");
  }

  @Test
  public void postIsCreatedSuccesfully() throws Exception {
    String auth = "Bearer " + accessTokenFor("normalUser", "password123!");

    MvcResult res = mockMvc.perform(post("/api/posts/")
        .header("Authorization", auth)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"content\":\"test post\"}"))
        .andExpect(status().isOk())
        .andReturn();

    int postId = JsonPath.read(res.getResponse().getContentAsString(), "$.id");
    mockMvc.perform(get("/api/posts/" + postId).header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("test post"));
  }

  @Test
  public void postsOfFollowedUsers() throws Exception {
    // create users to follow
    String encodedPw = encoder.encode("password123!");

    User posterUser1 = new User("posterUser1", "posterUser1@test.com", encodedPw);
    posterUser1.getRoles().add(roleRepository.findByName(ERole.ROLE_USER).get());
    User posterUser2 = new User("posterUser2", "posterUser2@test.com", encodedPw);
    posterUser2.getRoles().add(roleRepository.findByName(ERole.ROLE_USER).get());

    userRepository.save(posterUser1);
    userRepository.save(posterUser2);

    // create posts for those users
    Post post1 =
        new Post(
            new Date(),
            "test post 1",
            userRepository.findById(posterUser1.getId()).get());

    Post post2 =
        new Post(
            new Date(),
            "test post 2",
            userRepository.findById(posterUser2.getId()).get());

    postRepository.save(post1);
    postRepository.save(post2);

    // follow those users
    String auth = "Bearer " + accessTokenFor("normalUser", "password123!");

    mockMvc.perform(post("/api/users/" + posterUser1.getId() + "/follow")
        .header("Authorization", auth))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/users/" + posterUser2.getId() + "/follow")
        .header("Authorization", auth))
        .andExpect(status().isOk());

    // posts of followed users returned correctly
    MvcResult res = mockMvc.perform(get("/api/posts/").header("Authorization", auth))
        .andExpect(status().isOk())
        .andReturn();

    String content = res.getResponse().getContentAsString();
    Assert.isTrue(content.contains("test post 1"),
        "Response content has to include 'test post 1'");
    Assert.isTrue(content.contains("test post 2"),
        "Response content has to include 'test post 2'");

    // unfollow
    mockMvc.perform(post("/api/users/" + posterUser1.getId() + "/unfollow")
        .header("Authorization", auth))
        .andExpect(status().isOk());

    res = mockMvc.perform(get("/api/posts/").header("Authorization", auth))
        .andExpect(status().isOk())
        .andReturn();
    content = res.getResponse().getContentAsString();
    Assert.isTrue(!content.contains("test post 1"),
        "Response content should not include 'test post 1'");

  }
}

