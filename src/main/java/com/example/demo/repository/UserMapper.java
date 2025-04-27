package com.example.demo.repository;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.example.demo.mode.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
@DS("master")
public interface UserMapper {

    @Select("select * from users where username = #{username}")
    UserEntity findByUsername(String username);

    @Insert("Insert into users (username,password,role) values (#{username},#{password},#{role})")
    void addUser(UserEntity user);

    @Update("UPDATE users SET password = #{password}<if test='role' != null'>, role = #{role}</if> WHERE id = #{id}")
    void update(UserEntity user);

    @Update("UPDATE users SET  role = #{role} WHERE id = #{id}")
    void updateRole(UserEntity user);

    @Update("UPDATE users SET password = #{password} WHERE username = #{username}")
    int updatePassword(UserEntity user);

    @Select("select * from users")
    List<UserEntity> getAll();
}