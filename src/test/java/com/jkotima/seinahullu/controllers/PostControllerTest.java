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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.jayway.jsonpath.JsonPath;
import com.jkotima.seinahullu.models.ERole;
import com.jkotima.seinahullu.models.User;
import com.jkotima.seinahullu.repository.PostRepository;
import com.jkotima.seinahullu.repository.RoleRepository;
import com.jkotima.seinahullu.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
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

  @BeforeAll
  public void setup() {
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
}

