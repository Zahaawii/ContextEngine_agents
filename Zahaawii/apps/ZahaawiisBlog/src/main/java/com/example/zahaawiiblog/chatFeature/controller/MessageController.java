package com.example.zahaawiiblog.chatFeature.controller;

import com.example.zahaawiiblog.chatFeature.entity.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class MessageController {


    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required for chat messages");
        }

        chatMessage.setSender(principal.getName());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required for chat");
        }

        chatMessage.setSender(principal.getName());
        var attrs = headerAccessor.getSessionAttributes();
        if(attrs != null) {
            attrs.put("sender", principal.getName());
        }
        return chatMessage;
    }

}
