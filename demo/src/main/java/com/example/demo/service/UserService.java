package com.example.demo.service;

import com.example.demo.data.Message;
import com.example.demo.data.Post;
import com.example.demo.data.User;
import com.example.demo.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;

public interface UserService extends UserDetailsService {

    Set<User> getAll();

    User get(int id);

    User getByEmail(String email) throws InvalidCredentialsException;

    User getByUsername(String username) throws InvalidCredentialsException;

    void register(String username, String password, String email) throws RegistrationException;

    void subscribeTo(String username, String subscribeToUsername) throws InvalidCredentialsException, UsernameNotFoundException;

    void unsubscribeFrom(String username, String unsubscribeFromUsername) throws InvalidCredentialsException, UsernameNotFoundException;

    Message sendFriendRequest(String usernameToAdd, String usernameToAsk) throws InvalidCredentialsException, UsernameNotFoundException;

    Message denyFriendRequest(String usernameToAdd, String usernameToAsk) throws InvalidCredentialsException, UsernameNotFoundException, NotFriendsException;

    Message acceptFriendRequest(String usernameToAdd, String usernameToAsk) throws InvalidCredentialsException, UsernameNotFoundException, NotFriendsException;

    Message unfriend(String usernameToRemove, String usernameToRemoveFrom) throws InvalidCredentialsException, UsernameNotFoundException;

    Message sendMessage(String usernameFrom, String usernameTo, String content) throws InvalidCredentialsException, UsernameNotFoundException;

    Page<Post> getPosts(String forUsername,String fromUsername, Pageable pageable) throws InvalidCredentialsException, UsernameNotFoundException, NotSubscribedException;

    int addPost(String username, String header, String content) throws InvalidCredentialsException, UsernameNotFoundException;

    void removePost(int id);

    Page<Message> getMessageHistory(String username1, String username2, Pageable pageable) throws UsernameNotFoundException ;

    Page<Post> getFeed(String username, Pageable pageable) throws InvalidCredentialsException;
}
