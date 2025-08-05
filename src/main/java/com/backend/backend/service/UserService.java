package com.backend.backend.service;

import com.backend.backend.model.Role;
import com.backend.backend.model.User;
import com.backend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class UserService {

    @Autowired private UserRepository repo;
    @Autowired private PasswordEncoder passwordEncoder;   // supplied by SecurityConfig
    @Autowired private JavaMailSender mailSender;

    /** token → email (memory-only; replace with DB table in production) */
    private final Map<String, String> resetTokens = new ConcurrentHashMap<>();

    /* ---------- SIGN-UP ---------- */
    public User signup(String username, String email, String rawPassword) {
        if (repo.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        if (repo.existsByEmail(email))      throw new IllegalArgumentException("Email already exists");

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));   // BCrypt (or PBKDF2) hash
        u.setRole(Role.USER);
        return repo.save(u);
    }

    /* ---------- LOGIN (username OR email) ---------- */
    public User login(String identifier, String rawPassword) {
        User user = repo.findByUsername(identifier);
        if (user == null) user = repo.findByEmail(identifier);
        
        System.out.println("Login attempt: " + identifier);
        System.out.println("User found: " + (user != null));
        System.out.println("Entered password: " + rawPassword);
        System.out.println("Stored hash: " + (user != null ? user.getPassword() : "null"));
        if (user != null) {
            System.out.println("Password match: " + passwordEncoder.matches(rawPassword, user.getPassword()));
        }
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return user;
    }

    /* ---------- CHANGE PASSWORD (while logged in) ---------- */
    public void changePassword(String username, String oldPw, String newPw) {
        User user = repo.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("User not found");
        if (!passwordEncoder.matches(oldPw, user.getPassword())) {
            throw new IllegalArgumentException("Old password incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPw));
        repo.save(user);
    }

    /* ---------- SEND USERNAME TO REGISTERED EMAIL ---------- */
    public void sendUsernameReminder(String email) {
        User user = repo.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("Email not registered");

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Your EasyLearn username");
        msg.setText("Hello!\n\nYour username is: " + user.getUsername());
        mailSender.send(msg);
    }

    /* ---------- CREATE & EMAIL PASSWORD-RESET TOKEN ---------- */
    public void sendPasswordResetToken(String email) {
        System.out.println("[DEBUG] Request to send password reset to: " + email);

        User user = repo.findByEmail(email);
        if (user == null) {
            System.out.println("[ERROR] No user found with email: " + email);
            throw new IllegalArgumentException("Email not registered");
        }

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);  // store token
        System.out.println("[DEBUG] Generated token: " + token + " for email: " + email);

        String link = "https://easylearn.com/reset.html?token=" + token; // you can change to localhost during testing
        System.out.println("[DEBUG] Reset URL: " + link);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Reset your EasyLearn password");
        msg.setText("Click the link below to reset your password:\n" + link);

        try {
            mailSender.send(msg);
            System.out.println("[SUCCESS] Password reset email sent to: " + email);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send email to: " + email);
            e.printStackTrace();
            throw new RuntimeException("Failed to send email. Check logs for details.");
        }
    }


    /* ---------- CONSUME TOKEN & SET NEW PASSWORD ---------- */
    public void resetPassword(String token, String newPw) {
        String email = resetTokens.remove(token);          // consume once
        if (email == null) throw new IllegalArgumentException("Invalid or expired token");

        User user = repo.findByEmail(email);
        if (user == null) throw new IllegalStateException("User not found");

        user.setPassword(passwordEncoder.encode(newPw));
        repo.save(user);
    }
    
    
    public User getByUsername(String username) {
        return repo.findByUsername(username);
    }

}
