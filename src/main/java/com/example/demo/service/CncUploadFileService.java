package com.example.demo.service;

import com.example.demo.mode.CncFileDetail;
import com.example.demo.mode.CncUploadFile;
import com.example.demo.repository.CncFileDetailMapper;
import com.example.demo.repository.CncUploadFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CncUploadFileService {
    int createFileRecord(CncUploadFile record);

    int updateFileRecord(CncUploadFile record);

    int deleteFileRecord(Integer id);

    CncUploadFile getFileById(Integer id);

    List<CncUploadFile> getFileByProductCode(String productCode);

    List<CncUploadFile> getAllFiles();
}

@Service
@Transactional
class CncUploadFileServiceImpl implements CncUploadFileService {

    private final CncUploadFileMapper mapper;
    private final CncFileDetailMapper detailMapper;

    @Autowired
    public CncUploadFileServiceImpl(CncUploadFileMapper mapper, CncFileDetailMapper detailMapper) {
        this.mapper = mapper;
        this.detailMapper = detailMapper;
    }

    @Transactional
    public int createFileRecord(CncUploadFile record) {
        int a = mapper.insert(record);
        for (CncFileDetail fileDetail : record.getDetails()) {
            fileDetail.setCncUploadId(record.getId());
            System.out.println("插入行数: " + a + ", 回填ID: " + record.getId());
            fileDetail.setState("0");
            detailMapper.insert(fileDetail);
        }
        return 0;
    }

    @Override
    public int updateFileRecord(CncUploadFile record) {
        if (mapper.selectByPrimaryKey(record.getId()) == null) {
            throw new RuntimeException("文件记录不存在");
        }
        return mapper.updateByPrimaryKey(record);
    }

    @Override
    public int deleteFileRecord(Integer id) {
        CncUploadFile exist = mapper.selectByPrimaryKey(id);
        if (exist == null) return 0;
        return mapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CncUploadFile getFileById(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CncUploadFile> getFileByProductCode(String productCode) {
        return mapper.selectByProductCode(productCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CncUploadFile> getAllFiles() {
        return mapper.selectAll();
    }
}