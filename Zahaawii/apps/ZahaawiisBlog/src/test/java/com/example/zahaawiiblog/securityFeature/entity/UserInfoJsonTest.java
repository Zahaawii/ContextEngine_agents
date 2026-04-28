package com.example.zahaawiiblog.securityFeature.entity;

import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UserInfoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void passwordIsWriteOnlyDuringSerialization() throws Exception {
        UserInfo user = UserInfo.builder()
                .userId(1L)
                .name("zahaawii")
                .email("z@example.com")
                .password("secret")
                .roles("USER")
                .build();

        String json = objectMapper.writeValueAsString(user);

        assertFalse(json.contains("password"));
    }

}
