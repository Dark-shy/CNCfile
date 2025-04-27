package com.example.demo.repository;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.example.demo.mode.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@DS("master")
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 根据用户名查找用户
    UserEntity findByUsername(String username);
}
