package com.example.demo.service;

import com.example.demo.mode.amadaFile;
import com.example.demo.repository.fileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    private Timestamp data = new Timestamp(2003 - 1900, 0, 0, 0, 0, 0, 0);
    private int sum = 0;
    @Autowired
    private fileMapper fileMapper;

    public List<amadaFile> findFile(String id) {
        List<amadaFile> amadaFileList = fileMapper.fileList();
        List<amadaFile> amadaFiles = new ArrayList<>();
        for (amadaFile file : amadaFileList) {
            if (id.equals("admin") && file.getState().equals("2"))
                amadaFiles.add(file);
            else if (file.getCategoryId().equals(id) && !file.getState().equals("2"))
                amadaFiles.add(file);
        }
        return amadaFiles;
    }

    public String addFile(amadaFile file) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        file.setAddTime(timestamp);
        if (timestamp.toLocalDateTime().toLocalDate().isEqual(data.toLocalDateTime().toLocalDate())) {
            file.setVersions(timestamp.toLocalDateTime().toLocalDate() + "_" + String.format("%02d", sum));
            file.setProcedureName("AM_" + file.getCategoryId() + "_" + timestamp.toLocalDateTime().toLocalDate() + "_" + String.format("%02d", sum++));
        } else {
            sum = 0;
            file.setVersions(timestamp.toLocalDateTime().toLocalDate() + "_" + String.format("%02d", sum));
            file.setProcedureName("AM_" + file.getCategoryId() + "_" + timestamp.toLocalDateTime().toLocalDate() + "_" + String.format("%02d", sum++));
        }
        data = timestamp;
        fileMapper.addFile(file);
        return file.getProcedureName();
    }

    public String updateState(String fileId) {
        return "" + fileMapper.updateState(fileId);
    }

    public String update(String fileId, String state) {
        return "" + fileMapper.update(fileId, state);
    }
}
