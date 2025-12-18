package com.apartmentcommunity.booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceClient {
    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(@Value("${user.service.url:http://localhost:8085}") String userServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.userServiceUrl = userServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public Optional<SessionInfo> getSessionInfo(String token) {
        try {
            String url = userServiceUrl + "/api/session/" + token;
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                SessionInfo sessionInfo = new SessionInfo();
                sessionInfo.setUserId(((Number) body.get("userId")).longValue());
                sessionInfo.setUsername((String) body.get("username"));
                sessionInfo.setRole((String) body.getOrDefault("role", "USER"));
                return Optional.of(sessionInfo);
            }
        } catch (HttpClientErrorException e) {
            // Session not found or invalid
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error calling user service: " + e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public Optional<UserInfo> getUserInfo(Long userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, (Class<Map<String, Object>>) (Class<?>) Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                UserInfo userInfo = new UserInfo();
                userInfo.setId(((Number) body.get("id")).longValue());
                userInfo.setUsername((String) body.get("username"));
                userInfo.setName((String) body.get("name"));
                userInfo.setFlatNo((String) body.get("flatNo"));
                userInfo.setContactNumber((String) body.get("contactNumber"));
                userInfo.setRole((String) body.getOrDefault("role", "USER"));
                return Optional.of(userInfo);
            }
        } catch (HttpClientErrorException e) {
            // User not found
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error calling user service: " + e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    public static class SessionInfo {
        private Long userId;
        private String username;
        private String role;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isAdmin() {
            return "ADMIN".equals(role);
        }
    }

    public static class UserInfo {
        private Long id;
        private String username;
        private String name;
        private String flatNo;
        private String contactNumber;
        private String role;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFlatNo() {
            return flatNo;
        }

        public void setFlatNo(String flatNo) {
            this.flatNo = flatNo;
        }

        public String getContactNumber() {
            return contactNumber;
        }

        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}

