package com.example.demo.repository;

import com.example.demo.mode.CncDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CncDetailMapper {
    @Insert("INSERT INTO cnc_detail (user_name, scattered_type)" +
            "VALUES (#{userName}, #{scatteredType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addScattered(CncDetail cncDetail);

    @Select("select * from cnc_detail")
    @Results({
            @Result(property = "userName", column = "user_name"),
            @Result(property = "scatteredType", column = "scattered_type")
    })
    List<CncDetail> getList();

    @Select("WITH LatestCNCFile AS (\n" +
            "    SELECT\n" +
            "        cfd.*,\n" +
            "        cuf.product_code,\n" +
            "        ROW_NUMBER() OVER (\n" +
            "            PARTITION BY cfd.scattered_type\n" +
            "            ORDER BY cuf.apply_date DESC,cuf.id DESC\n" +
            "            ) AS rn\n" +
            "    FROM cnc_file_detail cfd\n" +
            "             INNER JOIN cnc_upload_file cuf\n" +
            "                        ON cfd.cnc_upload_id = cuf.id\n" +
            "    WHERE cuf.product_code like CONCAT(#{productCode}, '%') \n" +
            ")\n" +
            "SELECT\n" +
            "    cuf.product_code,\n" +
            "    cuf.product_name,\n" +
            "    cuf.cad_file_name,\n" +
            "    cuf.apply_date,\n" +
            "    cuf.specification,\n" +
            "    cuf.model_name,\n" +
            "    cuf.engineer,\n" +
            "    lcf.id,\n" +
            "    lcf.scattered_type,\n" +
            "    lcf.version,\n" +
            "    lcf.cnc_ard_program,\n" +
            "    lcf.suspend_reason,\n" +
            "    lcf.state\n" +
            "FROM cnc_detail cd\n" +
            "         LEFT JOIN LatestCNCFile lcf\n" +
            "                   ON cd.scattered_type = lcf.scattered_type\n" +
            "                       AND lcf.rn = 1\n" +
            "         LEFT JOIN cnc_upload_file cuf\n" +
            "                   ON lcf.cnc_upload_id = cuf.id\n" +
            "WHERE cuf.product_code IS NOT NULL;")
    List<Map<String, Object>> get(String productCode);
}
