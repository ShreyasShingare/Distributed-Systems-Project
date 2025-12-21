package com.apartmentcommunity.user.controller;

import com.apartmentcommunity.user.dto.LoginRequest;
import com.apartmentcommunity.user.dto.RegisterRequest;
import com.apartmentcommunity.user.model.Session;
import com.apartmentcommunity.user.model.User;
import com.apartmentcommunity.user.service.SessionService;
import com.apartmentcommunity.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final SessionService sessionService;

    @Autowired
    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                request.getUsername(), 
                request.getPassword(),
                request.getName(),
                request.getFlatNo(),
                request.getContactNumber()
            );
            String token = sessionService.createSession(user.getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole() != null ? user.getRole() : "USER");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty() || !userService.validatePassword(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userOpt.get();
        String token = sessionService.createSession(user.getId());
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole() != null ? user.getRole() : "USER");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session/{token}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String token) {
        Optional<Session> sessionOpt = sessionService.getSessionByToken(token);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Session session = sessionOpt.get();
        Optional<User> userOpt = userService.findById(session.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole() != null ? user.getRole() : "USER");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("name", user.getName());
        response.put("flatNo", user.getFlatNo());
        response.put("contactNumber", user.getContactNumber());
        response.put("role", user.getRole() != null ? user.getRole() : "USER");
        return ResponseEntity.ok(response);
    }
}

