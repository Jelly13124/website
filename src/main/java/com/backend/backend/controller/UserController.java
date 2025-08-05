package com.backend.backend.controller;

import com.backend.backend.model.User;
import com.backend.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        User user = userService.signup(username, email, password);
        return ResponseEntity.ok(Map.of(
            "message", "Signup successful",
            "username", user.getUsername(),
            "role", user.getRole().toString()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userService.login(username, password);
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "username", user.getUsername(),
            "role", user.getRole().toString()
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String oldPw = body.get("oldPassword");
        String newPw = body.get("newPassword");

        userService.changePassword(username, oldPw, newPw);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/recover-username")
    public ResponseEntity<?> recoverUsername(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        userService.sendUsernameReminder(email);
        return ResponseEntity.ok(Map.of("message", "Username reminder sent"));
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<?> sendResetLink(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        userService.sendPasswordResetToken(email);
        return ResponseEntity.ok(Map.of("message", "Password reset link sent"));
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
    
    @GetMapping("/get/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        User user = userService.getByUsername(username);
        return ResponseEntity.ok(Map.of(
              "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole().toString()
        ));
    }
    
}
