package com.example.demo.repository;

import com.example.demo.data.Message;
import com.example.demo.data.MessageType;
import com.example.demo.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    Set<Message> findAllByFromAndTo(User from, User to);

    Page<Message> findAllByFromInAndToIn(Set<User> user1, Set<User> user2, Pageable pageable);

    default Page<Message> findAllByFromInAndToIn(Set<User> users, Pageable pageable){
        return findAllByFromInAndToIn(users, users, pageable);
    }
    Message findFirstByFromAndToAndTypeOrderByDateTimeDesc(User from, User to, MessageType type);
}
