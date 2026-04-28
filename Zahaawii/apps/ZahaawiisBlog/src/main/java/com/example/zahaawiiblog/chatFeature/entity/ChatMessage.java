package com.example.zahaawiiblog.chatFeature.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ChatMessage {

    private MessageType type;
    private String content;
    private String sender;
}