package com.example.demo.mode;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CncDownloadFile {
    private Integer id;
    private Integer cncDetailId;    // 关联明细表ID
    private Timestamp downloadTime;  // 下载时间
    private String downloader;       // 下载人员
}