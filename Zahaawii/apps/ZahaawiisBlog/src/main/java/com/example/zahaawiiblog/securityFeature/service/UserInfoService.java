package com.example.zahaawiiblog.securityFeature.service;


import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.example.zahaawiiblog.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
public class UserInfoService implements UserDetailsService {


    private final UserRepository repository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserInfoService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional
    public void uploadImage(Long id, String imgPath) {
        if(repository.findUserByUserId(id) == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        UserInfo findUser = repository.findUserByUserId(id);
        findUser.setImgPath(imgPath);
        repository.save(findUser);
    }

    public UserInfo loadDomainUserByUsername(String username) {
        return repository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws
            UsernameNotFoundException {
        UserInfo userInfo = loadDomainUserByUsername(username);
        return User.withUsername(userInfo.getName())
                .password(userInfo.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
    }

    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "User added succesfully";
    }
}
