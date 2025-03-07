package com.hnq.e_commerce.controllers;


import com.hnq.e_commerce.dto.ApiResponse;
import com.hnq.e_commerce.dto.CategoryDto;
import com.hnq.e_commerce.entities.Category;
import com.hnq.e_commerce.services.CategoryService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {


    CategoryService categoryService;

    @GetMapping("/{id}")
    public ApiResponse<Category> getCategoryById(@PathVariable(value = "id", required = true) UUID categoryId) {
        Category category = categoryService.getCategory(categoryId);
        return ApiResponse.<Category>builder().build();

    }

    @GetMapping
    public ApiResponse<List<Category>> getAllCategories(HttpServletResponse response) {
        List<Category> categoryList = categoryService.getAllCategory();
        response.setHeader("Content-Range", String.valueOf(categoryList.size()));
        return ApiResponse.<List<Category>>builder().result(categoryList).build();

    }


    @PostMapping
    public ApiResponse<Category> createCategory(@RequestBody CategoryDto categoryDto) {
        Category category = categoryService.createCategory(categoryDto);
        return ApiResponse.<Category>builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .result(category)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> updateCategory(@RequestBody CategoryDto categoryDto, @PathVariable(value = "id", required = true) UUID categoryId) {
        Category updatedCategory = categoryService.updateCategory(categoryDto, categoryId);
        return ApiResponse.<Category>builder().result(updatedCategory).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable(value = "id", required = true) UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponse.<Void>builder().build();
    }
}