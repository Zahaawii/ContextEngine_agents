package com.example.zahaawiiblog.chatFeature.controller;

import com.example.zahaawiiblog.chatFeature.entity.ChatMessage;
import com.example.zahaawiiblog.chatFeature.entity.MessageType;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.security.Principal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageControllerTest {

    private final MessageController controller = new MessageController();

    @Test
    void sendMessageUsesAuthenticatedPrincipalAsSender() {
        ChatMessage message = ChatMessage.builder()
                .type(MessageType.CHAT)
                .content("hello")
                .sender("forged")
                .build();

        ChatMessage response = controller.sendMessage(message, principal("alice"));

        assertEquals("alice", response.getSender());
        assertEquals("hello", response.getContent());
    }

    @Test
    void addUserStoresPrincipalInSessionAttributes() {
        ChatMessage message = ChatMessage.builder()
                .type(MessageType.JOIN)
                .sender("forged")
                .build();
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(new HashMap<>());

        ChatMessage response = controller.addUser(message, accessor, principal("alice"));

        assertEquals("alice", response.getSender());
        assertEquals("alice", accessor.getSessionAttributes().get("sender"));
    }

    @Test
    void sendMessageRejectsMissingPrincipal() {
        ChatMessage message = ChatMessage.builder()
                .type(MessageType.CHAT)
                .content("hello")
                .build();

        assertThrows(IllegalStateException.class, () -> controller.sendMessage(message, null));
    }

    private static Principal principal(String name) {
        return () -> name;
    }
}
