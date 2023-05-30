package com.example.demo.data;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "SM_User")
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private LocalDateTime registerDateTime;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author", cascade = CascadeType.ALL)
    private Set<Post> posts = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="UserFriendRequests",
            joinColumns={@JoinColumn(name="FollowerId")},
            inverseJoinColumns={@JoinColumn(name="OwnerId")})
    private Set<User> incomingFriendRequests = new HashSet<>();

    @ManyToMany(mappedBy = "incomingFriendRequests", cascade = CascadeType.ALL)
    private Set<User> pendingRequests = new HashSet<>();

    @ManyToMany(mappedBy = "subscribedTo", cascade = CascadeType.ALL)
    private Set<User> subscribers = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="UserSubs",
            joinColumns={@JoinColumn(name="FollowerId")},
            inverseJoinColumns={@JoinColumn(name="OwnerId")})
    private Set<User> subscribedTo = new HashSet<>();


    @ManyToMany(mappedBy = "friendTo", cascade = CascadeType.ALL)
    private Set<User> friendsWith = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name="UserFriends",
            joinColumns={@JoinColumn(name="FollowerId")},
            inverseJoinColumns={@JoinColumn(name="OwnerId")})
    private Set<User> friendTo = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "from", cascade = CascadeType.ALL)
    private Set<Message> messages = new HashSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(username, user.username) && Objects.equals(email, user.email) && Objects.equals(registerDateTime, user.registerDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
