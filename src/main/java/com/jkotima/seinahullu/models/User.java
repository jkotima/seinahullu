package com.jkotima.seinahullu.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
    })
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 20)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  @JsonIgnore
  @NotBlank
  @Size(max = 120)
  private String password;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @JsonIgnore
  @ManyToMany
  private List<User> follows;
  
  @JsonIgnore
  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<Post> posts;

  public User() {}

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }
}
