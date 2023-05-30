package com.example.demo.controller;

import com.example.demo.data.Message;
import com.example.demo.data.Post;
import com.example.demo.dto.PostDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.exception.RegistrationException;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
public class RESTController {

    private final UserService userService;

    @Autowired
    public RESTController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO user){

        try {
            userService.register(user.username, user.password, user.email);
            return new ResponseEntity<>(String.valueOf(userService.getByUsername(user.username).getId()), HttpStatus.OK);
        }
        catch (RegistrationException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/subscribe")
    public ResponseEntity<String> subscribeTo(Principal principal, @RequestParam String subscribeTo){

        try {
            String username = getUsernameFromToken(principal);
            userService.subscribeTo(username,subscribeTo);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<String> unsubscribeFrom(Principal principal, @RequestParam String unsubscribeFrom){

        try {
            String username = getUsernameFromToken(principal);
            userService.unsubscribeFrom(username,unsubscribeFrom);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/friend_request")
    public ResponseEntity<String> sendFriendRequest(Principal principal, @RequestParam String sendTo){

        try {
            String username = getUsernameFromToken(principal);
            Message message = userService.sendFriendRequest(username,sendTo);
            return new ResponseEntity<>(message.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/friend_request")
    public ResponseEntity<String> acceptFriendRequest(Principal principal, @RequestParam String add){

        try {
            String username = getUsernameFromToken(principal);
            Message message = userService.acceptFriendRequest(add,username);
            return new ResponseEntity<>(message.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/friend_request")
    public ResponseEntity<String> denyFriendRequest(Principal principal, @RequestParam String deny){

        try {
            String username = getUsernameFromToken(principal);
            Message message = userService.denyFriendRequest(deny,username);
            return new ResponseEntity<>(message.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/friend")
    public ResponseEntity<String> unfriend(Principal principal, @RequestParam String remove){

        try {
            String username = getUsernameFromToken(principal);
            Message message = userService.unfriend(remove,username);
            return new ResponseEntity<>(message.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user")
    public ResponseEntity<String> sendMessage(Principal principal, @RequestParam String to){

        try {
            String username = getUsernameFromToken(principal);
            Message message = userService.sendMessage(username,to,"test content");
            return new ResponseEntity<>(message.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/post")
    public ResponseEntity<String> getPosts(Principal principal,
                                           @RequestParam(required = false) String user,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "3") int size,
                                           @RequestParam(defaultValue = "desc") String sort){

        try {

            String username = getUsernameFromToken(principal);

            List<Order> orders = new ArrayList<>();
            orders.add(new Order(getSortDirection(sort), "date"));


            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
            Page<Post> posts = userService.getPosts(username, user, pagingSort);

            Map<String, Object> response = new HashMap<>();
            response.put("posts", posts.getContent());
            response.put("currentPage", posts.getNumber());
            response.put("totalItems", posts.getTotalElements());
            response.put("totalPages", posts.getTotalPages());

            return new ResponseEntity<>(response.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    @PostMapping("/post")
    public ResponseEntity<String> addPost(@RequestBody PostDTO post, Principal principal){
        try {
            String username = getUsernameFromToken(principal);
            int post_id = userService.addPost(username, post.header,post.content);
            return new ResponseEntity<>(String.valueOf(post_id),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/post")
    public ResponseEntity<String> removePost(@RequestParam int id){

        try {
            userService.removePost(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<String> getMessageHistory(Principal principal,
                                                    @RequestParam String with,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "5") int size,
                                                    @RequestParam(defaultValue = "desc") String sort){

        try {
            String username = getUsernameFromToken(principal);

            List<Order> orders = new ArrayList<>();
            orders.add(new Order(getSortDirection(sort), "dateTime"));

            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
            Page<Message> messages = userService.getMessageHistory(username, with, pagingSort);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages.getContent());
            response.put("currentPage", messages.getNumber());
            response.put("totalItems", messages.getTotalElements());
            response.put("totalPages", messages.getTotalPages());

            return new ResponseEntity<>(response.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<String> getFeed(Principal principal,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "3") int size,
                                          @RequestParam(defaultValue = "desc") String sort){
        try {
            String username = getUsernameFromToken(principal);


            List<Order> orders = new ArrayList<>();
            orders.add(new Order(getSortDirection(sort), "date"));

            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
            Page<Post> feed = userService.getFeed(username, pagingSort);

            Map<String, Object> response = new HashMap<>();
            response.put("posts", feed.getContent());
            response.put("currentPage", feed.getNumber());
            response.put("totalItems", feed.getTotalElements());
            response.put("totalPages", feed.getTotalPages());

            return new ResponseEntity<>(response.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){

        try {
            userService.register("test", "test", "test");
            userService.register("test1", "test1", "test1");
            Message message = userService.sendMessage("test","test1","test content");

            return new ResponseEntity<>(message.toString(),HttpStatus.OK);
        }
        catch (Throwable e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private String getUsernameFromToken(Principal principal){
        if (principal==null) throw new BadCredentialsException("Invalid auth token passed");
        return principal.getName();
    }

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

}
