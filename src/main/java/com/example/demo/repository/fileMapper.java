package com.example.demo.repository;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.example.demo.mode.amadaFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
@DS("master")
public interface fileMapper {
    @Select("select * from amadafile where state != 4")
    List<amadaFile> fileList();

    @Insert("insert into amadafile (categoryId, categoryName, specification, addUser, engineer, addTime, versions," +
            "                       procedureName, type, state)" +
            "values (#{categoryId}, #{categoryName}, #{specification}, #{addUser}, #{engineer}, #{addTime}, #{versions}," +
            "                     #{procedureName}, #{type}, #{state});")
    int addFile(amadaFile file);

    @Update("update amadafile set state = CASE WHEN state = 0 THEN 1 WHEN state = 1 THEN 0 ELSE state END where fileId = #{fileId}")
    int updateState(String fileId);

    @Update("update amadafile set state = #{state} where fileId = #{fileId}")
    int update(String fileId, String state);
}
