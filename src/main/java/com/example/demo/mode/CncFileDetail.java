package com.example.demo.mode;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class CncFileDetail {
    private Integer id;
    private Integer cncUploadId;     // uploadId
    private String scatteredType;   // 散板类型
    private String version;       // 版本
    private String cncArdProgram;  // CNC-Ard程序名
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Timestamp suspendTime;  // 挂起时间
    private String suspender;       // 挂起人员
    private String suspendReason;  // 挂起原因
    private String state;           // 状态
    private String remark;          // 备注

//    List<CncDownloadFile>
}
