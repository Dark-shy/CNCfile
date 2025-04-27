package com.example.demo.repository;

import com.example.demo.mode.CncFileDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CncFileDetailMapper {

    @Insert("INSERT INTO cnc_file_detail (cnc_upload_id, scattered_type, version, cnc_ard_program, " +
            "suspend_time, suspender, suspend_reason, state, remark) " +
            "VALUES (#{cncUploadId}, #{scatteredType}, #{version}, #{cncArdProgram}, " +
            "#{suspendTime, jdbcType=TIMESTAMP}, #{suspender}, #{suspendReason}, #{state}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CncFileDetail record);

    @Update({"<script>",
            "UPDATE cnc_file_detail",
            "<set>",
            "<if test='cncUploadId != null'> cnc_upload_id = #{cncUploadId}, </if>",
            "<if test='scatteredType != null'> scattered_type = #{scatteredType}, </if>",
            "<if test='version != null'> version = #{version}, </if>",
            "<if test='cncArdProgram != null'> cnc_ard_program = #{cncArdProgram}, </if>",
            "<if test='suspendTime != null'> suspend_time = #{suspendTime}, </if>",
            "<if test='suspender != null'> suspender = #{suspender}, </if>",
            "<if test='suspendReason != null'> suspend_reason = #{suspendReason}, </if>",
            "state = CASE WHEN state = 0 THEN 1 WHEN state = 1 THEN 0 ELSE state END",
            "<if test='remark != null'> remark = #{remark}, </if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"})
    int updateByPrimaryKey(CncFileDetail record);

    @Select("SELECT * FROM cnc_file_detail WHERE id = #{id}")
    @Results({
            @Result(property = "cncUploadId", column = "cnc_upload_id"),
            @Result(property = "scatteredType", column = "scattered_type"),
            @Result(property = "cncArdProgram", column = "cnc_ard_program"),
            @Result(property = "suspendTime", column = "suspend_time"),
            @Result(property = "suspendReason", column = "suspend_reason")
    })
    CncFileDetail selectByPrimaryKey(Integer id);

    @Select("SELECT * FROM cnc_file_detail WHERE cnc_upload_id = #{cncUploadId}")
    @Results({
            @Result(property = "cncUploadId", column = "cnc_upload_id"),
            @Result(property = "scatteredType", column = "scattered_type"),
            @Result(property = "cncArdProgram", column = "cnc_ard_program"),
            @Result(property = "suspendTime", column = "suspend_time"),
            @Result(property = "suspendReason", column = "suspend_reason")
    })
    List<CncFileDetail> selectByProductCode(Integer cncUploadId);

    @Select("SELECT * FROM cnc_file_detail WHERE cnc_ard_program LIKE CONCAT('Ard_', #{cncArdProgram}, '%')")
    @Results({
            @Result(property = "cncUploadId", column = "cnc_upload_id"),
            @Result(property = "scatteredType", column = "scattered_type"),
            @Result(property = "cncArdProgram", column = "cnc_ard_program"),
            @Result(property = "suspendTime", column = "suspend_time"),
            @Result(property = "suspendReason", column = "suspend_reason")
    })
    List<CncFileDetail> selectByCncArdProgram(String cncArdProgram);
}