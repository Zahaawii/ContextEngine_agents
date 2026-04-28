package com.example.zahaawiiblog.controller;


import com.example.zahaawiiblog.DTO.AuthResponseDTO;
import com.example.zahaawiiblog.DTO.SignupRequest;
import com.example.zahaawiiblog.logginFeature.service.LoggingService;
import com.example.zahaawiiblog.securityFeature.DTO.AuthResponse;
import com.example.zahaawiiblog.securityFeature.DTO.CurrentUserResponse;
import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.example.zahaawiiblog.securityFeature.Entity.AuthRequest;
import com.example.zahaawiiblog.securityFeature.service.JwtService;
import com.example.zahaawiiblog.securityFeature.service.UserInfoService;
import com.example.zahaawiiblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserInfoService service;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoggingService loggingService;

    @GetMapping("/getallusers")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        List<UserInfo> getAllUserInfos = userService.getAllUsers();
        return new ResponseEntity<>(getAllUserInfos, HttpStatus.OK);
    }

    @GetMapping("/getuserbyid/{userid}")
    public ResponseEntity<UserInfo> getUserById(@PathVariable int userid) {
        UserInfo findUserInfo = userService.getUserByUserId(userid);
        return new ResponseEntity<>(findUserInfo, HttpStatus.OK);
    }

    @GetMapping("/getuserbyname/{username}")
    public ResponseEntity<Optional<UserInfo>> getUserByName(@PathVariable String username) {
        Optional<UserInfo> findUser = userService.findUserByUsername(username);
        if(findUser.isPresent()) {
            return new ResponseEntity<>(findUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/createuser")
    public ResponseEntity<?> createUser(@RequestBody SignupRequest req) {
        if(userService.findUserByUsername(req.name()).isPresent()) {
            return new ResponseEntity<>("Username already exist", HttpStatus.BAD_REQUEST);
        }
        UserInfo u = new UserInfo(null, req.name(), req.email(),
                req.password(),Date.valueOf(LocalDate.now()) ,
                "USER", null, null);

        service.addUser(u);
        loggingService.log(u.getUserId(), u.getName() + ": was created", u.getName(), 1L);

        String token = jwtService.generateToken(req.name());
        long ttlSeconds = jwtService.getExpirationSeconds();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtService.buildAuthCookie(token).toString())
                .body(new AuthResponseDTO(u.getUserId(), u.getName(), ttlSeconds));
    }


    @DeleteMapping("/deleteuser/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable int userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            loggingService.log(0L, "Unauthorized delete user attempt", "anonymous", 4L);
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        Optional<UserInfo> user = userService.findUserByUsername(auth.getName());
        if (user.isEmpty()) {
            loggingService.log(0L, auth.getName() + ": Tried to delete user but current user record was missing", auth.getName(), 4L);
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        if (user.get().getUserId() != userId) {
            loggingService.log(user.get().getUserId(), auth.getName() + ": Tried to delete a different user account", user.get().getName(), 4L);
            return new ResponseEntity<>("You are not allowed to delete this user", HttpStatus.FORBIDDEN);
        }

        if(userService.deleteUserById(userId) == -1) {
            loggingService.log(user.get().getUserId(), auth.getName() +": Tried to delete user but failed", user.get().getName(), 4L);
            return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
        }

        loggingService.log(user.get().getUserId(), auth.getName() + ": Deleted user succesfully", user.get().getName(), 3L);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtService.clearAuthCookie().toString())
                .body("User with id " + userId + " has been deleted");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        Optional<UserInfo> user = userService.findUserByUsername(authentication.getName());

        String token = jwtService.generateToken(authRequest.getUsername());
        long ttlSeconds = jwtService.getExpirationSeconds();
        loggingService.log(user.get().getUserId(), user.get().getName() + ": logged in to the system", authentication.getName(), 7L);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtService.buildAuthCookie(token).toString())
                .body(new AuthResponse(authRequest.getUsername(), ttlSeconds));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAuthenticated(auth)) {
            userService.findUserByUsername(auth.getName()).ifPresent(user ->
                    loggingService.log(user.getUserId(), user.getName() + ": logged out of the system", user.getName(), 7L)
            );
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, jwtService.clearAuthCookie().toString())
                .build();
    }

    @GetMapping("/auth/me")
    public ResponseEntity<CurrentUserResponse> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return userService.findUserByUsername(auth.getName())
                .map(user -> ResponseEntity.ok(new CurrentUserResponse(user.getUserId(), user.getName(), user.getImgPath())))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("/auth/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(Map.of(
                "token", csrfToken.getToken(),
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName()
        ));
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

}
