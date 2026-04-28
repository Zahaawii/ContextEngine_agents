package com.example.zahaawiiblog.securityFeature.config;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Base64;

public class GenKey {
    public static void main(String[] args) {
        SecretKey key = Jwts.SIG.HS256.key().build();
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    }
}
