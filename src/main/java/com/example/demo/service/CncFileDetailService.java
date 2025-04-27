package com.example.demo.service;

import com.example.demo.mode.CncFileDetail;
import com.example.demo.repository.CncFileDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CncFileDetailService {
    int createDetailRecord(CncFileDetail record);

    int updateDetailRecord(CncFileDetail record);

    CncFileDetail getDetailById(Integer id);

    List<CncFileDetail> getDetailsByProduct(Integer productCode);

    List<CncFileDetail> getByCncArdProgram(String cncArdProgram);
}

@Service
@Transactional
class CncFileDetailServiceImpl implements CncFileDetailService {

    private final CncFileDetailMapper mapper;

    @Autowired
    public CncFileDetailServiceImpl(CncFileDetailMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int createDetailRecord(CncFileDetail record) {
        return mapper.insert(record);
    }

    @Override
    public int updateDetailRecord(CncFileDetail record) {
        if (mapper.selectByPrimaryKey(record.getId()) == null) {
            throw new RuntimeException("明细记录不存在");
        }
        return mapper.updateByPrimaryKey(record);
    }


    @Override
    @Transactional(readOnly = true)
    public CncFileDetail getDetailById(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CncFileDetail> getDetailsByProduct(Integer productCode) {
        return mapper.selectByProductCode(productCode);
    }

    @Override
    public List<CncFileDetail> getByCncArdProgram(String cncArdProgram) {
        return mapper.selectByCncArdProgram(cncArdProgram);
    }
}