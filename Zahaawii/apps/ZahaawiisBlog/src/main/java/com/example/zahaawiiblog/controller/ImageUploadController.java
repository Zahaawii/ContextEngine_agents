package com.example.zahaawiiblog.controller;


import com.example.zahaawiiblog.securityFeature.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RequestMapping("/api/v1/uploads")
@RestController
@CrossOrigin(origins = "*")
public class ImageUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final UserInfoService userInfoService;

    public ImageUploadController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @PostMapping("/images/{userid}")
    public ResponseEntity<String> uploadImage(@PathVariable Long userid, @RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        try {
            String username = auth.getName();
            if (userInfoService.loadDomainUserByUsername(username).getUserId() != userid) {
                return new ResponseEntity<>("You are not allowed to upload for another user", HttpStatus.FORBIDDEN);
            }

            String filePath = saveImage(file, username);
            userInfoService.uploadImage(userid, filePath);
            return ResponseEntity.ok("Image uploaded succesfully: " + filePath);
        } catch (IOException e) {
            return new ResponseEntity<>("File not uploaded", HttpStatus.BAD_REQUEST);
        }
    }

    public String saveImage(MultipartFile file, String username) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if(!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = username + ".jpeg";
        Path filePath = uploadPath.resolve(fileName).normalize();
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }
}
