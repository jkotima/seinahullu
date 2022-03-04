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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.jayway.jsonpath.JsonPath;
import com.jkotima.seinahullu.models.ERole;
import com.jkotima.seinahullu.models.User;
import com.jkotima.seinahullu.repository.RoleRepository;
import com.jkotima.seinahullu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class TestControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @BeforeEach
  public void init() {
    userRepository.deleteAll();
    String encodedPw = encoder.encode("password123!");

    User normalUser = new User("normalUser", "normalUser@test.com", encodedPw);
    normalUser.getRoles().add(roleRepository.findByName(ERole.ROLE_USER).get());

    User moderatorUser = new User("moderatorUser", "moderatorUser@test.com", encodedPw);
    moderatorUser.getRoles().add(roleRepository.findByName(ERole.ROLE_MODERATOR).get());

    User adminUser = new User("adminUser", "adminUser@test.com", encodedPw);
    adminUser.getRoles().add(roleRepository.findByName(ERole.ROLE_ADMIN).get());

    userRepository.save(normalUser);
    userRepository.save(moderatorUser);
    userRepository.save(adminUser);
  }

  private String accessTokenFor(String username, String password) throws Exception {
    MvcResult res = mockMvc.perform(post("/api/auth/signin")
        .contentType(MediaType.APPLICATION_JSON)
        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password)))
        .andReturn();
    
    return JsonPath.read(res.getResponse().getContentAsString(), "$.accessToken");
  }

  @Test
  public void withoutLogin() throws Exception {
    mockMvc.perform(get("/api/test/all")).andExpect(status().isOk());
    mockMvc.perform(get("/api/test/user")).andExpect(status().is4xxClientError());
    mockMvc.perform(get("/api/test/mod")).andExpect(status().is4xxClientError());
    mockMvc.perform(get("/api/test/admin")).andExpect(status().is4xxClientError());
  }

  @Test
  public void withNormalUserLogin() throws Exception {
    String auth = "Bearer " + accessTokenFor("normalUser", "password123!");

    mockMvc.perform(get("/api/test/all").header("Authorization", auth))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/test/user").header("Authorization", auth))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/test/mod").header("Authorization", auth))
        .andExpect(status().is4xxClientError());
    mockMvc.perform(get("/api/test/admin").header("Authorization", auth))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void withModeratorUserLogin() throws Exception {
    String auth = "Bearer " + accessTokenFor("moderatorUser", "password123!");

    mockMvc.perform(get("/api/test/all").header("Authorization", auth))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/test/user").header("Authorization", auth))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/test/mod").header("Authorization", auth))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/test/admin").header("Authorization", auth))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void withAdminUserLogin() throws Exception {
    String auth = "Bearer " + accessTokenFor("adminUser", "password123!");

    mockMvc.perform(get("/api/test/all").header("Authorization", auth))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/test/user").header("Authorization", auth))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/test/mod").header("Authorization", auth))
        .andExpect(status().is4xxClientError());
    mockMvc.perform(get("/api/test/admin").header("Authorization", auth))
        .andExpect(status().isOk());
  }
}
