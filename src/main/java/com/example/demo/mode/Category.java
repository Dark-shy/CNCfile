package com.example.demo.mode;

import lombok.Data;

import javax.persistence.Table;


@Table(name = "MES_PART_VIEW")
@Data
public class Category {
    String categoryId;
    String categoryName;
    String specification;

    public Category() {
    }
}
