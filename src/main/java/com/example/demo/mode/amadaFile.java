package com.example.demo.mode;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class amadaFile {
    private int fileId;
    private String categoryId;
    private String categoryName;
    private String specification;
    private String addUser;
    private String engineer;
    private Timestamp addTime;
    private String versions;
    private String procedureName;
    private String type;
    private String state;
}
