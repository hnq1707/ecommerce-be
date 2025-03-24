package com.hnq.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDataDTO {
    private String id; // Ở đây dùng tên danh mục làm id hiển thị
    private int value;
    private String color;
}
