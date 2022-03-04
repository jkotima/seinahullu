package com.jkotima.seinahullu.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jkotima.seinahullu.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  
  @EntityGraph(attributePaths = {"roles"})
  Optional<User> findById(Long id);
  
  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);
}
