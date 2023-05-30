package com.example.demo.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User from;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    private User to;
    private String content;

    private MessageType type;

    public Message(User from, User to, LocalDateTime dateTime, String content, MessageType type) {
        this.from = from;
        this.dateTime = dateTime;
        this.to = to;
        this.content = content;
        this.type = type;
    }
}
