package com.jkotima.seinahullu.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jkotima.seinahullu.models.Post;
import com.jkotima.seinahullu.models.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  List<Post> findByUser(User user);
}
