package com.jkotima.seinahullu.controllers;

import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import com.jkotima.seinahullu.payload.request.ReplaceUserRequest;
import com.jkotima.seinahullu.payload.response.MessageResponse;
import com.jkotima.seinahullu.repository.RoleRepository;
import com.jkotima.seinahullu.repository.UserRepository;
import com.jkotima.seinahullu.models.ERole;
import com.jkotima.seinahullu.models.Role;
import com.jkotima.seinahullu.models.User;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @PutMapping("/{id}")
  @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
  public ResponseEntity<?> replaceUser(
      @Valid @RequestBody ReplaceUserRequest req,
      @PathVariable Long id) {

    User user = userRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Error: no such a user ID"));

    user.setUsername(req.getUsername());
    user.setPassword(encoder.encode(req.getPassword()));
    user.setEmail(req.getEmail());

    if (req.getRoles() != null) {
      Set<Role> roles = new HashSet<>();
      req.getRoles().forEach(role -> {
        switch (role) {
          case "admin":
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).get();
            roles.add(adminRole);
            break;
          case "mod":
            Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR).get();
            roles.add(modRole);
            break;
          default:
            Role userRole = roleRepository.findByName(ERole.ROLE_USER).get();
            roles.add(userRole);
        }
      });

      user.setRoles(roles);
    }

    return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
  }
}