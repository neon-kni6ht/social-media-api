package com.example.demo.service;

import com.example.demo.data.Message;
import com.example.demo.data.MessageType;
import com.example.demo.data.Post;
import com.example.demo.data.User;
import com.example.demo.exception.AlreadyRegisteredException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.NotFriendsException;
import com.example.demo.exception.NotSubscribedException;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService{

    UserRepository userRepository;
    PostRepository postRepository;
    MessageRepository messageRepository;

    PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PostRepository postRepository, MessageRepository messageRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Set<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User get(int id) throws UsernameNotFoundException{
        return userRepository.findById(id).orElseThrow(()->new UsernameNotFoundException("User with id " + id + " was not found"));
    }

    @Override
    public User getByEmail(String email) throws InvalidCredentialsException, UsernameNotFoundException {

        if (email==null || email.equals(""))
            throw new InvalidCredentialsException("email cannot be empty");

        return userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User with email " + email + " was not found"));
    }

    @Override
    public User getByUsername(String username) throws InvalidCredentialsException, UsernameNotFoundException {

        if (username==null || username.equals(""))
            throw new InvalidCredentialsException("username cannot be empty");

        return userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User with username " + username + " was not found"));
    }

    @Override
    public void register(String username, String password, String email) throws AlreadyRegisteredException {

        try {
            getByUsername(username);
            throw new AlreadyRegisteredException("User with such username is already present");
        }
        catch (UsernameNotFoundException ignored){}

        try {
            getByUsername(email);
            throw new AlreadyRegisteredException("User with such email is already present");
        }
        catch (UsernameNotFoundException ignored){}

        if (password==null || password.equals(""))
            throw new InvalidCredentialsException("password cannot be empty");


        User user = new User(username,passwordEncoder.encode(password),email);
        userRepository.save(user);
    }

    @Override
    public void subscribeTo(String username, String subscribeToUsername) throws InvalidCredentialsException, UsernameNotFoundException{

        if (username.equals(subscribeToUsername)) throw new NotSubscribedException("Can not subscribe to self");

        User subscriber = getByUsername(username);
        User owner = getByUsername(subscribeToUsername);

        subscriber.getSubscribedTo().add(owner);
        owner.getSubscribers().add(subscriber);

        userRepository.save(owner);

    }

    @Override
    public void unsubscribeFrom(String username, String unsubscribeFromUsername) throws InvalidCredentialsException, UsernameNotFoundException, NotSubscribedException{

        User subscriber = getByUsername(username);
        User owner = getByUsername(unsubscribeFromUsername);

        subscriber.getSubscribedTo().remove(owner);
        owner.getSubscribers().remove(subscriber);

        userRepository.save(owner);

    }

    @Override
    @Transactional
    public Message sendFriendRequest(String usernameToAdd, String usernameToAsk) throws InvalidCredentialsException, UsernameNotFoundException, NotFriendsException{

        if (usernameToAdd.equals(usernameToAsk)) throw new NotFriendsException("Can not send friend request to self");

        User userToAdd = getByUsername(usernameToAdd);
        User userToAsk = getByUsername(usernameToAsk);

        subscribeTo(usernameToAdd, usernameToAsk);

        Message message = new Message(userToAdd,userToAsk, LocalDateTime.now(),null, MessageType.FRIEND_REQUEST);

        userToAdd.getMessages().add(message);

        userToAdd.getPendingRequests().add(userToAsk);
        userToAsk.getIncomingFriendRequests().add(userToAdd);

        userRepository.save(userToAdd);

        return messageRepository.findFirstByFromAndToAndTypeOrderByDateTimeDesc(userToAdd,userToAsk,MessageType.FRIEND_REQUEST);
    }

    @Override
    @Transactional
    public Message acceptFriendRequest(String usernameToAdd, String usernameToAsk) throws UsernameNotFoundException, InvalidCredentialsException, NotFriendsException {

        User userToAdd = getByUsername(usernameToAdd);
        User userToAsk = getByUsername(usernameToAsk);

        if (!userToAsk.getIncomingFriendRequests().contains(userToAdd)) throw new NotFriendsException ("No friend request from " + usernameToAdd + " was found");

        Message message = new Message(userToAsk,userToAdd, LocalDateTime.now(),null, MessageType.FRIEND_APPROVE);

        userToAsk.getMessages().add(message);

        userToAdd.getPendingRequests().remove(userToAsk);
        userToAsk.getIncomingFriendRequests().remove(userToAdd);
        userToAdd.getFriendTo().add(userToAsk);
        userToAdd.getFriendsWith().add(userToAsk);
        userToAsk.getFriendTo().add(userToAdd);
        userToAsk.getFriendsWith().add(userToAdd);
        userRepository.save(userToAsk);

        subscribeTo(usernameToAsk,usernameToAdd);

        return messageRepository.findFirstByFromAndToAndTypeOrderByDateTimeDesc(userToAsk,userToAdd,MessageType.FRIEND_APPROVE);
    }

    @Override
    public Message denyFriendRequest(String usernameToAdd, String usernameToAsk) throws InvalidCredentialsException, UsernameNotFoundException, NotFriendsException{
        User userToAdd = getByUsername(usernameToAdd);
        User userToAsk = getByUsername(usernameToAsk);

        if (!userToAsk.getIncomingFriendRequests().contains(userToAdd)) throw new NotFriendsException ("No friend request from " + usernameToAdd + " was found");

        Message message = new Message(userToAsk,userToAdd, LocalDateTime.now(),null, MessageType.FRIEND_DENY);

        userToAsk.getMessages().add(message);

        userToAdd.getPendingRequests().remove(userToAsk);
        userToAsk.getIncomingFriendRequests().remove(userToAdd);

        userRepository.save(userToAsk);

        return messageRepository.findFirstByFromAndToAndTypeOrderByDateTimeDesc(userToAsk,userToAdd,MessageType.FRIEND_DENY);
    }


    @Override
    @Transactional
    public Message unfriend(String usernameToRemove, String usernameToRemoveFrom) throws UsernameNotFoundException, InvalidCredentialsException, NotFriendsException{
        User userToRemove = getByUsername(usernameToRemove);
        User userToAsk = getByUsername(usernameToRemoveFrom);

        if (!userToRemove.getFriendsWith().contains(userToAsk)) throw new NotFriendsException("Not friends with " + usernameToRemoveFrom);

        Message message = new Message(userToAsk,userToRemove, LocalDateTime.now(),null, MessageType.FRIEND_REMOVE);

        userToAsk.getMessages().add(message);

        userToAsk.getFriendsWith().remove(userToRemove);
        userToAsk.getFriendTo().remove(userToRemove);
        userToRemove.getFriendTo().remove(userToAsk);
        userToRemove.getFriendsWith().remove(userToAsk);

        userRepository.save(userToAsk);

        unsubscribeFrom(usernameToRemoveFrom,usernameToRemove);

        return messageRepository.findFirstByFromAndToAndTypeOrderByDateTimeDesc(userToAsk,userToRemove,MessageType.FRIEND_REMOVE);
    }


    @Override
    public Message sendMessage(String usernameFrom, String usernameTo, String content) throws UsernameNotFoundException, InvalidCredentialsException{

        if (usernameFrom.equals(usernameTo)) throw new InvalidCredentialsException("Can not send message to self");

        if (content == null || content.equals("")) throw new IllegalArgumentException("Not a valid message");

        User userFrom = getByUsername(usernameFrom);
        User userTo = getByUsername(usernameTo);

        Message message = new Message(userFrom,userTo,LocalDateTime.now(), content, MessageType.MESSAGE);

        userFrom.getMessages().add(message);

        userRepository.save(userFrom);

        return messageRepository.findFirstByFromAndToAndTypeOrderByDateTimeDesc(userFrom,userTo,MessageType.MESSAGE);
    }

    @Override
    public int addPost(String username, String header, String content) throws InvalidCredentialsException, UsernameNotFoundException, IllegalArgumentException {
        User user = getByUsername(username);
        if (header==null || header.equals("") || content==null || content.equals(""))
            throw new IllegalArgumentException("Invalid post data provided");
        Post post = new Post(LocalDateTime.now(),header,content,user);
        user.getPosts().add(post);

        userRepository.save(user);

        return postRepository.findFirstByAuthorOrderByDateDesc(getByUsername(username)).getId();
    }

    @Override
    public void removePost(int id) {
        postRepository.deleteById(id);
    }

    @Override
    public Page<Post> getPosts(String forUsername,String fromUsername, Pageable pageable) throws InvalidCredentialsException, UsernameNotFoundException, NotSubscribedException{

        User forUser = getByUsername(forUsername);

        if(fromUsername==null || fromUsername.equals(""))
            return postRepository.findAllByAuthorOrderByDateDesc(forUser, pageable);

        User fromUser = getByUsername(fromUsername);

        if (!forUser.getSubscribedTo().contains(fromUser))
            throw new NotSubscribedException("User " + fromUser +" is not in subscriptions");

        return postRepository.findAllByAuthorOrderByDateDesc(fromUser, pageable);
    }

    @Override
    public Page<Message> getMessageHistory(String username1, String username2, Pageable pageable) throws InvalidCredentialsException, UsernameNotFoundException {
        User user1 = getByUsername(username1);
        User user2 = getByUsername(username2);

        Set<User> users = new HashSet<>();
        users.add(user1);
        users.add(user2);

        return messageRepository.findAllByFromInAndToIn(users, pageable);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow();
    }

    @Override
    public Page<Post> getFeed(String username, Pageable pageable) throws InvalidCredentialsException, UsernameNotFoundException{

        User user = getByUsername(username);

        return postRepository.findAllByAuthorIn(user.getSubscribedTo(), pageable);
    }

}
