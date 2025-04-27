package com.example.demo.service;

import com.example.demo.mode.CncDownloadFile;
import com.example.demo.repository.CncDownloadFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface CncDownloadService {
    int recordDownload(CncDownloadFile record);

    List<CncDownloadFile> getDownloadHistory(Integer detailId);

    List<Map<String, Object>> getHistory(String productCode);
}

@Service
@Transactional
class CncDownloadServiceImpl implements CncDownloadService {

    private final CncDownloadFileMapper mapper;

    @Autowired
    public CncDownloadServiceImpl(CncDownloadFileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int recordDownload(CncDownloadFile record) {
        return mapper.insert(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CncDownloadFile> getDownloadHistory(Integer detailId) {
        return mapper.selectByDetailId(detailId);
    }

    @Override
    public List<Map<String, Object>> getHistory(String productCode) {
        return mapper.selectDownloadHistory(productCode);
    }
}
