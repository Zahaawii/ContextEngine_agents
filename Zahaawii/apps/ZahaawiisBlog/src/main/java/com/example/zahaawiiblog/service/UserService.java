package com.example.zahaawiiblog.service;

import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.example.zahaawiiblog.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfo getUserByUserId(long id) {
        return userRepository.findUserByUserId(id);
    }

    public List<UserInfo> getAllUsers() {
        return (List<UserInfo>) userRepository.findAll();
    }

    public void createNewUser(UserInfo userInfo) {
        userRepository.save(userInfo);
    }

    public Optional<UserInfo> findUserByUsername(String username) {
        return userRepository.findByName(username);
    }

    public long deleteUserById(long userId) {
        if(getUserByUserId(userId) != null) {
            userRepository.deleteById(userId);
            return 1;
        }
        return -1;
    }
}
