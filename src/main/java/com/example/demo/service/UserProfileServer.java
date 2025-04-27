package com.example.demo.service;

import com.example.demo.mode.UserProfile;
import com.example.demo.repository.UserProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserProfileServer {
    @Autowired
    UserProfileMapper userProfileMapper;

    public UserProfile find(String workcode) {
        List<UserProfile> userProfiles = userProfileMapper.getUserProfile();
        if (userProfiles == null) {
            return null;
        }
        for (UserProfile i : userProfiles) {
            if (Objects.equals(i.getWorkCode(), workcode)) {
                return i;
            }
        }
        return null;
    }

    public List<UserProfile> getAll() {
        return userProfileMapper.getUserProfile();
    }
}
