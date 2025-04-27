package com.example.demo.service;

import com.example.demo.mode.Category;
import com.example.demo.repository.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public Category find(String id) {
        List<Category> categories = categoryMapper.getCategory();
        if (categories == null) {
            return null;
        }
        for (Category i : categories) {
            if (i.getCategoryId().equals(id))
                return i;
        }
        return null;
    }
}
