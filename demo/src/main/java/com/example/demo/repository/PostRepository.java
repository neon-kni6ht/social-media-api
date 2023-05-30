package com.example.demo.repository;

import com.example.demo.data.Post;
import com.example.demo.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Page<Post> findAllByAuthorOrderByDateDesc(User author, Pageable pageable);

    Post findFirstByAuthorOrderByDateDesc(User author);

    Page<Post> findAllByAuthorIn(Set<User> authors, Pageable pageable);
}
