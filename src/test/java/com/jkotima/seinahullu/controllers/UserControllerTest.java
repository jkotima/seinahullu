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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.jayway.jsonpath.JsonPath;
import com.jkotima.seinahullu.models.ERole;
import com.jkotima.seinahullu.models.User;
import com.jkotima.seinahullu.repository.RoleRepository;
import com.jkotima.seinahullu.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @BeforeAll
  public void setup() {
    userRepository.deleteAll();
    String encodedPw = encoder.encode("password123!");

    User normalUser = new User("normalUser", "normalUser@test.com", encodedPw);
    normalUser.getRoles().add(roleRepository.findByName(ERole.ROLE_USER).get());

    User adminUser = new User("adminUser", "adminUser@test.com", encodedPw);
    adminUser.getRoles().add(roleRepository.findByName(ERole.ROLE_ADMIN).get());

    userRepository.save(normalUser);
    userRepository.save(adminUser);
  }

  private String accessTokenFor(String username, String password) throws Exception {
    MvcResult res = mockMvc.perform(post("/api/auth/signin")
        .contentType(MediaType.APPLICATION_JSON)
        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password)))
        .andExpect(status().isOk())
        .andReturn();

    return JsonPath.read(res.getResponse().getContentAsString(), "$.accessToken");
  }

  @Test
  public void userCanReplaceOwnData() throws Exception {
    String auth = "Bearer " + accessTokenFor("normalUser", "password123!");
    Long id = userRepository.findByUsername("normalUser").get().getId();

    mockMvc.perform(put("/api/users/" + id)
        .header("Authorization", auth)
        .contentType(MediaType.APPLICATION_JSON)
        .content(String.format(
            "{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\"}",
            "newusername",
            "newpassword123!",
            "newemail@email.com")))
        .andExpect(status().isOk());
   
    Assert.isTrue(userRepository.existsByUsername("newusername"), "username exists in db");
    Assert.isTrue(userRepository.existsByEmail("newemail@email.com"), "email exists in db");
    accessTokenFor("newusername", "newpassword123!");
  }
}
