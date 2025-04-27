package com.example.demo.mode;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CncUploadFile {
    private Integer id;
    private String applicant;       // 申请人
    private String department;         //部门
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")// 申请部门
    private Timestamp applyDate;   // 申请日期
    private String engineer;       // 工程师
    private String productCode;    // 品号
    private String productName;    // 品名
    private String specification; // 规格
    private String modelName;      // 机种名
    private String plateThickness;  // 板材厚度
    private String plateMaterial;  // 板材材质
    private Integer versionType;    // 版本类型
    private String cadFileName;    // CAD图纸文件名

    // 关联查询扩展（根据需要添加）
    private List<CncFileDetail> details;
}
