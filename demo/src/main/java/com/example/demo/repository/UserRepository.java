package com.example.demo.repository;

import com.example.demo.data.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    Set<User> findAll();

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
