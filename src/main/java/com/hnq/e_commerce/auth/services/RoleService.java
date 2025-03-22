package com.hnq.e_commerce.auth.services;

import com.hnq.e_commerce.auth.dto.request.PermissionRequest;
import com.hnq.e_commerce.auth.dto.request.RoleRequest;
import com.hnq.e_commerce.auth.dto.response.RoleResponse;
import com.hnq.e_commerce.auth.repositories.PermissionRepository;
import com.hnq.e_commerce.auth.repositories.RoleRepository;
import com.hnq.e_commerce.mapper.RoleMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

        // Lấy danh sách tên permission từ request (vì Permission entity dùng name làm @Id)
        var permissionNames = request.getPermissions()
                .stream()
                .map(PermissionRequest::getName)
                .collect(Collectors.toList());
        var permissions = permissionRepository.findAllById(permissionNames);
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    public RoleResponse update(String id, RoleRequest request) {
        var roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role with id " + id + " not found.");
        }
        var role = roleOpt.get();

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        var permissionNames = request.getPermissions()
                .stream()
                .map(PermissionRequest::getName)
                .collect(Collectors.toList());
        var permissions = permissionRepository.findAllById(permissionNames);
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}
