package com.example.demo.service;

import com.example.demo.mode.CncDetail;
import com.example.demo.repository.CncDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface CncDetailService {

    List<CncDetail> getCncDetail();

    void setCncDetail(CncDetail cncDetail);

    List<Map<String, Object>> getCndFileHistory(String productCode);

}

@Service
class CncDetailServiceImpl implements CncDetailService {
    private final CncDetailMapper mapper;

    @Autowired
    public CncDetailServiceImpl(CncDetailMapper mapper) {
        this.mapper = mapper;
    }


    @Override
    public List<CncDetail> getCncDetail() {
        return mapper.getList();
    }

    @Override
    public void setCncDetail(CncDetail cncDetail) {
        mapper.addScattered(cncDetail);
    }

    @Override
    public List<Map<String, Object>> getCndFileHistory(String productCode) {
        return mapper.get(productCode);
    }
}