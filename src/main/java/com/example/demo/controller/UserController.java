package com.example.demo.controller;

import com.example.demo.mode.UserEntity;
import com.example.demo.mode.UserProfile;
import com.example.demo.service.UserService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin(origins = "*")
@ResponseBody
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    @GetMapping("/user")
    public List<UserEntity> getUser() {
        return userService.getAll();
    }

    @GetMapping("/userprofile")
    public List<UserProfile> getUserProfile() {
        return userService.getAllProfile();
    }

    @PutMapping("/user")
    public void updateUser(@RequestBody UserEntity user) {
        userService.updateUser(user);
    }

    @PostMapping("/update/user")
    public String updatePassword(@RequestBody UserEntity user) {
        return String.valueOf(userService.updatePassword(user));
    }

    @PostMapping("/newuser")
    public void addUser(@RequestBody UserEntity user) {
        userService.addUser(user);
    }

    @GetMapping("/user/{id}")
    public UserProfile find(@PathVariable String id) {
        return userService.findUser(id);
    }

    @PostMapping("/user")
    public LoginResponse login(@RequestBody UserEntity user) {
        String role = userService.findUser(user);
        if (role == null) {
            return new LoginResponse(false, "null", "未找到相关用户");
        } else if (role.equals("0")) {
            return new LoginResponse(false, "null", "密码错误");
        } else {
            return new LoginResponse(true, role, user.getUsername());
        }
    }

    @Data
    public static class LoginResponse {
        private boolean success;
        private String role;
        private String message;

        public LoginResponse(boolean success, String role, String message) {
            this.success = success;
            this.role = role;
            this.message = message;
        }
    }
}