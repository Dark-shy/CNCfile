package com.example.demo.controller;

import com.example.demo.mode.Category;
import com.example.demo.mode.CncDetail;
import com.example.demo.service.CategoryService;
import com.example.demo.service.CncDetailService;
import com.example.demo.service.CncDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
@ResponseBody
public class FindCollect {
    private final CategoryService categoryService;
    private final CncDetailService cncDetailService;
    private final CncDownloadService cncDownloadService;

    @Autowired
    public FindCollect(CategoryService categoryService, CncDetailService cncDetailService, CncDownloadService cncDownloadService) {
        this.categoryService = categoryService;
        this.cncDetailService = cncDetailService;
        this.cncDownloadService = cncDownloadService;
    }


    @GetMapping("/category/{id}")
    public Category find(@PathVariable String id) {
        return categoryService.find(id);
    }

    @GetMapping("/cncfile/{id}")
    public List<Map<String, Object>> getCnc(@PathVariable String id) {
        return cncDetailService.getCndFileHistory(id);
    }

    @GetMapping("/history/{id}")
    public List<Map<String, Object>> getHistory(@PathVariable String id) {
        return cncDownloadService.getHistory(id);
    }

    @GetMapping("/detail")
    public List<CncDetail> get() {
        return cncDetailService.getCncDetail();
    }

    @PutMapping("/detail")
    public void setCncDetail(@RequestBody CncDetail cncDetail) {
        cncDetailService.setCncDetail(cncDetail);
    }
}
