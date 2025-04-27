package com.example.demo.mode;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.Data;

import javax.persistence.*;

// 用户实体类
@DS("master")
@Entity
@Table(name = "users")
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;
}

