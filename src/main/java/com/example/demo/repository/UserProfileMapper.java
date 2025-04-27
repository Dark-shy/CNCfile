package com.example.demo.repository;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.example.demo.mode.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
@DS("crew2")
public interface UserProfileMapper {
    @Select("select lastname as userName,DepartmentPath as userSector,workcode as workCode from dbo.MES_HrmResource_View")
    List<UserProfile> getUserProfile();

    @Select("select lastname as userName,DepartmentPath as userSector,workcode as workCode,OAid as oaId from dbo.MES_HrmResource_View where workcode = #{workCode}")
    UserProfile findUsername(String workCode);
}
