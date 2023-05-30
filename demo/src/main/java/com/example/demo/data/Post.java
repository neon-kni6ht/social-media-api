package com.example.demo.data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private int id;

    @Column(nullable = false)
    private LocalDateTime date;

    private String headline;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id", nullable=false)
    private User author;

    public Post(LocalDateTime date, String headline, String content, User author) {
        this.date = date;
        this.headline = headline;
        this.content = content;
        this.author = author;
    }
}
