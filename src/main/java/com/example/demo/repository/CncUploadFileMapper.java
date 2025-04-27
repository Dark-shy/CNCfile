package com.example.demo.repository;

import com.example.demo.mode.CncUploadFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CncUploadFileMapper {

    @Insert("INSERT INTO cnc_upload_file (applicant, department, apply_date, engineer, product_code, " +
            "product_name, specification, model_name, plate_thickness, plate_material, version_type, cad_file_name) " +
            "VALUES (#{applicant}, #{department}, #{applyDate}, #{engineer}, #{productCode}, #{productName}, " +
            "#{specification}, #{modelName}, #{plateThickness}, #{plateMaterial}, #{versionType}, #{cadFileName})")
    @Options(
            useGeneratedKeys = true,    // 启用自增主键
            keyProperty = "id",         // 映射到实体类的 id 字段
            keyColumn = "id"            // 数据库自增列名
    )
    int insert(CncUploadFile record);

    @Update("UPDATE cnc_upload_file SET " +
            "applicant = #{applicant}, department = #{department}, apply_date = #{applyDate}, " +
            "engineer = #{engineer}, product_code = #{productCode}, product_name = #{productName}, " +
            "specification = #{specification}, model_name = #{modelName}, plate_thickness = #{plateThickness}, " +
            "plate_material = #{plateMaterial}, version_type = #{versionType}, cad_file_name = #{cadFileName} " +
            "WHERE id = #{id}")
    int updateByPrimaryKey(CncUploadFile record);

    @Delete("DELETE FROM cnc_upload_file WHERE id = #{id}")
    int deleteByPrimaryKey(Integer id);

    @Select("SELECT * FROM cnc_upload_file WHERE id = #{id}")
    @Results({
            @Result(property = "applyDate", column = "apply_date"),
            @Result(property = "productCode", column = "product_code"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "modelName", column = "model_name"),
            @Result(property = "plateThickness", column = "plate_thickness"),
            @Result(property = "plateMaterial", column = "plate_material"),
            @Result(property = "versionType", column = "version_type"),
            @Result(property = "cadFileName", column = "cad_file_name")
    })
    CncUploadFile selectByPrimaryKey(Integer id);

    @Select("SELECT TOP 5000 * FROM cnc_upload_file WHERE product_code like CONCAT(#{productCode}, '%') order by apply_date DESC")
    @Results({
            // 复用字段映射（实际开发建议使用公共@ResultMap）
            @Result(property = "applyDate", column = "apply_date"),
            @Result(property = "productCode", column = "product_code"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "modelName", column = "model_name"),
            @Result(property = "plateThickness", column = "plate_thickness"),
            @Result(property = "plateMaterial", column = "plate_material"),
            @Result(property = "versionType", column = "version_type"),
            @Result(property = "cadFileName", column = "cad_file_name")
    })
    List<CncUploadFile> selectByProductCode(String productCode);

    @Select("SELECT * FROM cnc_upload_file")
    List<CncUploadFile> selectAll();
}