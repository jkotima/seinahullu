package com.jkotima.seinahullu.repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jkotima.seinahullu.models.Post;
import com.jkotima.seinahullu.models.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  Optional<List<Post>> findByUser(User user);
  Optional<List<Post>> findByUserIn(Collection<User> users);
}
