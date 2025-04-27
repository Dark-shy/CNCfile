package com.example.demo.repository;

import com.example.demo.mode.CncDownloadFile;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CncDownloadFileMapper {

    @Insert("INSERT INTO cnc_download_file (cnc_detail_id, download_time, downloader) " +
            "VALUES (#{cncDetailId}, #{downloadTime}, #{downloader})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CncDownloadFile record);

    @Select("SELECT * FROM cnc_download_file WHERE cnc_detail_id = #{detailId} ORDER BY download_time DESC")
    @Results({
            @Result(property = "cncDetailId", column = "cnc_detail_id"),
            @Result(property = "downloadTime", column = "download_time")
    })
    List<CncDownloadFile> selectByDetailId(Integer detailId);

    @Select("select top 5000 product_code,\n" +
            "       product_name,\n" +
            "       specification,\n" +
            "       scattered_type,\n" +
            "       engineer,\n" +
            "       apply_date,\n" +
            "       cad_file_name,\n" +
            "       cnc_ard_program,\n" +
            "       download_time,\n" +
            "       downloader\n" +
            "from cnc_download_file cdf\n" +
            "         left join cnc_file_detail cfd on cdf.cnc_detail_id = cfd.id\n" +
            "         left join cnc_upload_file cuf on cfd.cnc_upload_id = cuf.id\n" +
            "where cuf.product_code like CONCAT(#{productCode}, '%') order by download_time DESC")
    List<Map<String, Object>> selectDownloadHistory(String productCode);
}