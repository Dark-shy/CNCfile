package com.example.demo.repository;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.example.demo.mode.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
@DS("crew1")
public interface CategoryMapper {
    @Select("select IMAAL001 as categoryId,IMAAL003 as categoryName,IMAAL004 as specification from DSDATA.MES_PART_VIEW")
    List<Category> getCategory();
}
