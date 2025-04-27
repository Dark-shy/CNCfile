package com.example.demo.service;

import com.example.demo.mode.UserEntity;
import com.example.demo.mode.UserProfile;
import com.example.demo.repository.UserMapper;
import com.example.demo.repository.UserProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserProfileMapper userProfileMapper;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public static boolean validatePassword(String rawPassword, String encodedPassword) {
        // 创建 BCryptPasswordEncoder 实例
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 调用 matches 方法进行密码校验
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String findUser(UserEntity userEntity) {
        UserEntity user = userMapper.findByUsername(userEntity.getUsername());
        if (user == null) {
            return null;
        } else {
            if (validatePassword(userEntity.getPassword(), user.getPassword())) {
                return user.getRole();
            } else {
                return "0";
            }
        }
    }

    // 更新用户信息
    public boolean updateUser(UserEntity existingUser) {
        if (existingUser.getPassword().equals("********"))
            userMapper.updateRole(existingUser);
        else {
            String encodedPassword = passwordEncoder.encode(existingUser.getPassword());
            existingUser.setPassword(encodedPassword);
            userMapper.update(existingUser);
        }
        return true;
    }

    public void addUser(UserEntity userEntity) {
        // 对明文密码进行加密
        String encodedPassword = passwordEncoder.encode(userEntity.getPassword());
        userEntity.setPassword(encodedPassword);
        userMapper.addUser(userEntity);
    }

    public List<UserEntity> getAll() {
        return userMapper.getAll();
    }


    public int updatePassword(UserEntity entity) {
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return userMapper.updatePassword(entity);
    }

    public List<UserProfile> getAllProfile() {
        return userProfileMapper.getUserProfile();
    }

    public UserProfile findUser(String workCode) {
        return userProfileMapper.findUsername(workCode);
    }
}
