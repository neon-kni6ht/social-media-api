package com.example.demo.controller;

import com.example.demo.data.Message;
import com.example.demo.data.MessageType;
import com.example.demo.data.Post;
import com.example.demo.data.User;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.PostDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.exception.NotFriendsException;
import com.example.demo.exception.NotSubscribedException;
import com.example.demo.exception.RegistrationException;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RESTControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @InjectMocks
    private RESTController restController;

    @Test
    @DisplayName("Should return the feed with the given pagination and sorting parameters")
    void getFeedWithPaginationAndSorting() {
        User user1 = new User("user1", "password1", "user1@example.com");
        User user2 = new User("user2", "password2", "user2@example.com");
        User user3 = new User("user3", "password3", "user3@example.com");

        Post post1 = new Post(LocalDateTime.now(), "Headline 1", "Content 1", user1);
        Post post2 = new Post(LocalDateTime.now().minusDays(1), "Headline 2", "Content 2", user2);
        Post post3 = new Post(LocalDateTime.now().minusDays(2), "Headline 3", "Content 3", user3);

        List<Post> posts = Arrays.asList(post1, post2, post3);
        PageImpl<Post> page = new PageImpl<>(posts);

        when(principal.getName()).thenReturn(user1.getUsername());
        when(userService.getFeed(anyString(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<String> response = restController.getFeed(principal, 0, 3, "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("posts"));
        assertTrue(response.getBody().contains("currentPage"));
        assertTrue(response.getBody().contains("totalItems"));
        assertTrue(response.getBody().contains("totalPages"));
        assertTrue(response.getBody().contains("Headline 1"));
        assertTrue(response.getBody().contains("Headline 2"));
        assertTrue(response.getBody().contains("Headline 3"));

        verify(userService, times(1)).getFeed(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName(
            "Should return  message history when there are no messages between two users")
    void getMessageHistory() {
        String username = "user1";
        String with = "user2";
        User user1 = new User(username, "password", "email");
        User user2 = new User(with, "password", "email1");
        int page = 0;
        int size = 5;
        String sort = "desc";
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(user1, user2, LocalDateTime.now(), "message1", MessageType.MESSAGE));
        messages.add(new Message(user2, user1, LocalDateTime.now(), "message2", MessageType.MESSAGE));
        messages.add(new Message(user1, user2, LocalDateTime.now(), "message3", MessageType.MESSAGE));
        PageImpl<Message> pageImpl = new PageImpl<>(messages);
        when(principal.getName()).thenReturn(username);
        when(userService.getMessageHistory(eq(username), eq(with), any(Pageable.class)))
                .thenReturn(pageImpl);

        ResponseEntity<String> response =
                restController.getMessageHistory(principal, with, page, size, sort);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("messages"));
        assertTrue(response.getBody().contains("message1"));
        assertTrue(response.getBody().contains("message2"));
        assertTrue(response.getBody().contains("message3"));
        assertTrue(response.getBody().contains("currentPage"));
        assertTrue(response.getBody().contains("totalItems"));
        assertTrue(response.getBody().contains("totalPages"));
        verify(userService, times(1))
                .getMessageHistory(eq(username), eq(with), any(Pageable.class));
    }

    @Test
    @DisplayName("Should remove the post successfully when the post id is valid")
    void removePostWhenPostIdIsValid() {
        int postId = 1;
        doNothing().when(userService).removePost(postId);

        ResponseEntity<String> response = restController.removePost(postId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).removePost(postId);
    }

    @Test
    @DisplayName("Should throw an exception when the post id is not valid")
    void removePostWhenPostIdIsNotValidThenThrowException() {
        int postId = -1;
        doThrow(new IllegalArgumentException("Invalid post id")).when(userService).removePost(postId);

        ResponseEntity<String> response = restController.removePost(postId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid post id", response.getBody());
        verify(userService, times(1)).removePost(postId);
    }

    @Test
    @DisplayName("Should add a post successfully when valid data is provided")
    void addPostWithValidData() {
        PostDTO postDTO = new PostDTO("Test Header", "Test Content");
        String username = "testUser";
        int postId = 1;

        when(principal.getName()).thenReturn("testUser");
        when(userService.addPost(username, postDTO.header, postDTO.content))
                .thenReturn(postId);
        ResponseEntity<String> response = restController.addPost(postDTO, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.valueOf(postId), response.getBody());
        verify(userService, times(1))
                .addPost(eq(username), eq(postDTO.header), eq(postDTO.content));
    }

    @Test
    @DisplayName("Should return a bad request when invalid data is provided")
    void addPostWithInvalidData() {
        PostDTO postDTO = new PostDTO("", "");

        when(userService.addPost(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid post data"));
        when(principal.getName()).thenReturn("testUser");

        ResponseEntity<String> responseEntity = restController.addPost(postDTO, principal);

        verify(userService, times(1)).addPost(anyString(), anyString(), anyString());

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Invalid post data", responseEntity.getBody());
    }

    @Test
    @DisplayName("Should return an error when invalid pagination parameters are provided")
    void getPostsWhenInvalidPaginationParametersProvidedThenReturnError() {
        String user = "testUser";
        int page = -1;
        int size = 0;
        String sort = "asc";
        when(principal.getName()).thenReturn(user);

        ResponseEntity<String> response =
                restController.getPosts(principal, user, page, size, sort);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(0)).getPosts(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should return an error when an invalid user is provided")
    void getPostsWhenInvalidUserProvidedThenReturnError() {
        User user1 = new User("user1", "password1", "email1");

        LocalDateTime date1 = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(2022, 1, 2, 0, 0);
        LocalDateTime date3 = LocalDateTime.of(2022, 1, 3, 0, 0);

        Post post1 = new Post(date1, "Headline1", "Content1", user1);
        Post post2 = new Post(date2, "Headline2", "Content2", user1);
        Post post3 = new Post(date3, "Headline3", "Content3", user1);

        List<Post> posts = Arrays.asList(post1, post2, post3);

        when(principal.getName()).thenReturn("user1");
        when(userService.getPosts(anyString(), anyString(), any(Pageable.class)))
                .thenThrow(new NotSubscribedException("Not subscribed"));


        ResponseEntity<String> response = restController.getPosts(principal, "", 0, 3, "asc");

        verify(userService, times(1)).getPosts(anyString(), anyString(), any(Pageable.class));
        verify(userService, times(0)).getByUsername(anyString());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not subscribed", response.getBody());

    }

    @Test
    @DisplayName("Should return posts for the current user when no user parameter is provided")
    void getPostsForCurrentUserWhenNoUserParameterProvided() {
        User user1 = new User("user1", "password1", "email1");

        LocalDateTime date1 = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(2022, 1, 2, 0, 0);
        LocalDateTime date3 = LocalDateTime.of(2022, 1, 3, 0, 0);

        Post post1 = new Post(date1, "Headline1", "Content1", user1);
        Post post2 = new Post(date2, "Headline2", "Content2", user1);
        Post post3 = new Post(date3, "Headline3", "Content3", user1);

        List<Post> posts = Arrays.asList(post1, post2, post3);

        when(principal.getName()).thenReturn("user1");
        when(userService.getPosts(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(posts));


        ResponseEntity<String> response = restController.getPosts(principal, "", 0, 3, "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(userService, times(1)).getPosts(anyString(), anyString(), any(Pageable.class));
        verify(userService, times(0)).getByUsername(anyString());

        assertTrue(response.getBody().contains("Headline1"));
        assertTrue(response.getBody().contains("Headline2"));
        assertTrue(response.getBody().contains("Headline3"));
        assertTrue(response.getBody().contains("Content1"));
        assertTrue(response.getBody().contains("Content2"));
        assertTrue(response.getBody().contains("Content3"));
    }

    @Test
    @DisplayName("Should send a message successfully when valid inputs are provided")
    void sendMessageWithValidInputs() {
        String to = "testUser";
        String from = "testUsername";
        String content = "test content";
        MessageDTO messageDTO = new MessageDTO(content, "testUser");

        when(principal.getName()).thenReturn(from);
        when(userService.sendMessage(from, to, content))
                .thenReturn(new Message(new User(from, "pass", "email"),
                        new User(to, "pass2", "email2"),
                        LocalDateTime.now(), content,
                        MessageType.MESSAGE));

        ResponseEntity<String> response = restController.sendMessage(principal, messageDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).sendMessage(from, to, content);
    }

    @Test
    @DisplayName("Should throw an exception when sending a message from an invalid user")
    void sendMessageFromInvalidUserThrowsException() {
        String username = "testUser";
        String content = "test content";
        MessageDTO messageDTO = new MessageDTO(content, username);

        when(principal.getName()).thenReturn(username);
        when(userService.sendMessage(username, username, content))
                .thenThrow(new NotFriendsException("Can not send message to oneself"));

        ResponseEntity<String> response = restController.sendMessage(principal, messageDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Can not send message to oneself", response.getBody());
        verify(userService, times(1)).sendMessage(username, username, content);
    }

    @Test
    @DisplayName("Should return an error when trying to unfriend a user who is not a friend")
    void unfriendUserNotFriendThenReturnError() {
        String remove = "userToRemove";
        String username = "username";

        when(principal.getName()).thenReturn(username);
        when(userService.unfriend(remove, username))
                .thenThrow(new NotFriendsException("User is not a friend"));

        ResponseEntity<String> response = restController.unfriend(principal, remove);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User is not a friend", response.getBody());
        verify(userService, times(1)).unfriend(remove, username);
    }

    @Test
    @DisplayName("Should unfriend the user successfully")
    void unfriendUserSuccessfully() {
        String remove = "user1";
        String username = "user2";
        Message message =
                new Message(new User("user", "pass", "email"),
                        new User("user2", "pass2", "email2"),
                        LocalDateTime.now(), null,
                        MessageType.FRIEND_REMOVE);

        when(principal.getName()).thenReturn(username);
        when(userService.unfriend(remove, username)).thenReturn(message);

        ResponseEntity<String> response = restController.unfriend(principal, remove);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(message.toString(), response.getBody());
        verify(userService, times(1)).unfriend(remove, username);
    }

    @Test
    @DisplayName("Should deny the friend request and return a message")
    void denyFriendRequestAndReturnMessage() {
        String currentUsername = "user1";
        String friendUsername = "user2";

        User user1 = new User("user1", "password", "user1@example.com");
        User user2 = new User("user2", "password", "user2@example.com");

        Message message = new Message(user2, user1, LocalDateTime.now(), null, MessageType.FRIEND_DENY);

        when(principal.getName()).thenReturn(currentUsername);
        when(userService.denyFriendRequest(friendUsername, currentUsername)).thenReturn(message);

        ResponseEntity<String> response = restController.denyFriendRequest(principal, friendUsername);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).denyFriendRequest(friendUsername, currentUsername);
    }

    @Test
    @DisplayName("Should throw an exception when the friend request is not found")
    void denyFriendRequestWhenNotFoundThenThrowException() {
        String currentUsername = "user1";
        String friendUsername = "user2";

        when(principal.getName()).thenReturn(currentUsername);
        when(userService.denyFriendRequest(friendUsername, currentUsername)).thenThrow(new NotFriendsException("Friend request is not found"));

        ResponseEntity<String> response = restController.denyFriendRequest(principal, friendUsername);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request is not found", response.getBody());
        verify(userService, times(1)).denyFriendRequest(friendUsername, currentUsername);
    }

    @Test
    @DisplayName("Should accept the friend request and return a success message")
    void acceptFriendRequestSuccess() {
        String add = "testUser";
        String to = "currentUser";
        User currentUser = new User(to, "password", "email1@example.com");
        User friendUser = new User(add, "password", "email2@example.com");

        when(principal.getName()).thenReturn(to);
        when(userService.acceptFriendRequest(add, to))
                .thenReturn(new Message(friendUser, currentUser, LocalDateTime.now(), null, MessageType.FRIEND_REQUEST));

        ResponseEntity<String> response = restController.acceptFriendRequest(principal, add);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).acceptFriendRequest(add, to);
    }

    @Test
    @DisplayName("Should throw an exception when there is not friend request")
    void acceptFriendRequestError() {
        String add = "testUser";
        String username = "testUser2";
        when(principal.getName()).thenReturn(username);
        when(userService.acceptFriendRequest(add, username))
                .thenThrow(new NotFriendsException("No friend request found"));

        ResponseEntity<String> response = restController.acceptFriendRequest(principal, add);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No friend request found", response.getBody());
        verify(userService, times(1)).acceptFriendRequest(add, username);
    }

    @Test
    @DisplayName("Should throw an exception when sending a friend request to oneself")
    void sendFriendRequestWhenSendingToOneselfThenThrowException() {
        UserDTO userDTO = new UserDTO("testuser", "testpassword", "testemail@example.com");
        when(principal.getName()).thenReturn("testuser");
        when(userService.sendFriendRequest("testuser", "testuser")).thenThrow(new NotSubscribedException("Can not subscribe to oneself"));

        String username = userDTO.username;
        ResponseEntity<String> response = restController.sendFriendRequest(principal, userDTO.username);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Can not subscribe to oneself", response.getBody());
        verify(userService, times(1)).sendFriendRequest(username, username);
    }

    @Test
    @DisplayName("Should send a friend request successfully when the input is valid")
    void sendFriendRequestWhenInputIsValid() {

        User user1 = new User("username1", "password", "email1@example.com");
        User user2 = new User("username2", "password", "email2@example.com");
        when(principal.getName()).thenReturn("username1");
        when(userService.sendFriendRequest("username1", "username2")).thenReturn(new Message(user1, user2, LocalDateTime.now(), null, MessageType.FRIEND_REQUEST));

        ResponseEntity<String> response = restController.sendFriendRequest(principal, "username2");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).sendFriendRequest("username1", "username2");
    }

    @Test
    @DisplayName("Should throw an exception when sending a friend request to an invalid user")
    void sendFriendRequestWhenInvalidUserThenThrowException() {
        String sendTo = "invalidUser";
        String username = "validUser";
        when(principal.getName()).thenReturn(username);
        when(userService.sendFriendRequest(username, sendTo)).thenThrow(new RegistrationException("User not found"));

        ResponseEntity<String> response = restController.sendFriendRequest(principal, sendTo);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        verify(userService, times(1)).sendFriendRequest(username, sendTo);
    }

    @Test
    @DisplayName("Should successfully unsubscribe from a user")
    void unsubscribeFromUserSuccessfully() {
        String username = "testUser";
        String unsubscribeFrom = "userToUnsubscribe";
        doNothing().when(userService).unsubscribeFrom(username, unsubscribeFrom);
        when(principal.getName()).thenReturn(username);

        ResponseEntity<String> response = restController.unsubscribeFrom(principal, unsubscribeFrom);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).unsubscribeFrom(username, unsubscribeFrom);
    }

    @Test
    @DisplayName("Should throw an exception when unsubscribing from a non-existent user")
    void unsubscribeFromNonExistentUserThrowsException() {
        String username = "testUser";
        String unsubscribeFrom = "nonExistentUser";

        when(principal.getName()).thenReturn(username);
        doThrow(new RegistrationException("User " + unsubscribeFrom + " does not exist"))
                .when(userService).unsubscribeFrom(username, unsubscribeFrom);

        ResponseEntity<String> response = restController.unsubscribeFrom(principal, unsubscribeFrom);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User " + unsubscribeFrom + " does not exist", response.getBody());

        verify(userService, times(1)).unsubscribeFrom(username, unsubscribeFrom);
        verify(principal, times(1)).getName();
    }

    @Test
    @DisplayName("Should subscribe to the user successfully when the username is valid")
    void subscribeToWhenUsernameIsValid() {
        String username = "testUser";
        String subscribeTo = "subscribeToUser";
        doNothing().when(userService).subscribeTo(username, subscribeTo);
        when(principal.getName()).thenReturn("testUser");

        ResponseEntity<String> response = restController.subscribeTo(principal, subscribeTo);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).subscribeTo(username, subscribeTo);
    }

    @Test
    @DisplayName("Should throw an exception when the username is invalid")
    void subscribeToWhenUsernameIsInvalidThenThrowException() {
        UserDTO userDTO = new UserDTO("invalid_username", "password", "email@example.com");
        String subscribeTo = "valid_username";

        doThrow(new RegistrationException("Invalid username")).when(userService).subscribeTo(userDTO.username, subscribeTo);
        when(principal.getName()).thenReturn("invalid_username");

        ResponseEntity<String> response = restController.subscribeTo(principal, subscribeTo);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid username", response.getBody());

        verify(userService, times(1)).subscribeTo(userDTO.username, subscribeTo);
    }

    @Test
    @DisplayName(
            "Should return a bad request response with an error message when the registration fails")
    void registerUserWhenRegistrationFails() {
        UserDTO user = new UserDTO("testuser", "testpassword", "testemail@example.com");
        String errorMessage = "Registration failed";
        doThrow(new RegistrationException(errorMessage)).when(userService).register(user.username, user.password, user.email);

        ResponseEntity<String> response = restController.registerUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(userService, times(1)).register(user.username, user.password, user.email);
    }

    @Test
    @DisplayName(
            "Should register a new user and return the user ID when the registration is successful")
    void registerUserWhenRegistrationIsSuccessful() {
        UserDTO user = new UserDTO("testuser", "testpassword", "testemail@example.com");
        when(userService.getByUsername(user.username)).thenReturn(new User("testuser", "testpassword", "testemail@example.com"));

        ResponseEntity<String> response = restController.registerUser(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).register(user.username, user.password, user.email);
        verify(userService, times(1)).getByUsername(user.username);
    }
}