package com.hnq.e_commerce.auth.controllers;

import com.hnq.e_commerce.auth.dto.request.AssignRoleRequest;
import com.hnq.e_commerce.auth.dto.request.RoleRequest;
import com.hnq.e_commerce.auth.dto.response.RoleResponse;
import com.hnq.e_commerce.auth.services.RoleService;
import com.hnq.e_commerce.dto.request.RemoveRoleRequest;
import com.hnq.e_commerce.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @PutMapping("/{role}")
    ApiResponse<RoleResponse> update(@PathVariable String role,@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder().result(roleService.update(role, request)).build();
    }
    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role) {
        roleService.delete(role);
        return ApiResponse.<Void>builder().build();
    }
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> assignRoleToUser(@RequestBody AssignRoleRequest request) {
        roleService.assignRoleToUser(request);
        return ApiResponse.<Void>builder()
                .message("Role assigned successfully")
                .build();
    }
    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> removeRoleFromUser(@RequestBody RemoveRoleRequest request) {
        roleService.removeRoleFromUser(request);
        return ApiResponse.<Void>builder()
                .message("Role removed successfully")
                .build();
    }
}