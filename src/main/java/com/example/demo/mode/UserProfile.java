package com.example.demo.mode;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "MES_HrmResource_View")
public class UserProfile {
    String userName;
    @Column(name = "DepartmentPath")
    String userSector;
    @Column(name = "workcode")
    String workCode;
    @Column(name = "OAid")
    int oaId;
}
