package com.hnq.e_commerce.auth.services;

import com.hnq.e_commerce.auth.dto.request.AssignRoleRequest;
import com.hnq.e_commerce.auth.dto.request.PermissionRequest;
import com.hnq.e_commerce.auth.dto.request.RoleRequest;
import com.hnq.e_commerce.auth.dto.response.RoleResponse;
import com.hnq.e_commerce.auth.entities.Role;
import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.auth.repositories.PermissionRepository;
import com.hnq.e_commerce.auth.repositories.RoleRepository;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.dto.request.RemoveRoleRequest;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.mapper.RoleMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;
    UserRepository userRepository;

    @Transactional
    public RoleResponse create(RoleRequest request) {
        log.info("Tạo vai trò mới: {}", request.getName());

        // Kiểm tra đầu vào
        validateRoleRequest(request);

        // Kiểm tra xem vai trò đã tồn tại chưa
        if (roleRepository.existsById(request.getName())) {
            log.error("Vai trò với tên '{}' đã tồn tại", request.getName());
            throw new ResourceNotFoundEx(ErrorCode.ROLE_NOT_FOUND);
        }

        var role = roleMapper.toRole(request);

        // Lấy danh sách tên permission từ request (vì Permission entity dùng name làm @Id)
        var permissionNames = request.getPermissions()
                .stream()
                .map(PermissionRequest::getName)
                .collect(Collectors.toList());
        var permissions = permissionRepository.findAllById(permissionNames);

        // Kiểm tra xem tất cả các quyền đều tồn tại
        if (permissions.size() != permissionNames.size()) {
            log.error("Một số quyền không tồn tại trong hệ thống");
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        role.setPermissions(new HashSet<>(permissions));

        try {
            role = roleRepository.save(role);
            log.info("Đã tạo vai trò mới thành công: {}", role.getName());
            return roleMapper.toRoleResponse(role);
        } catch (Exception e) {
            log.error("Lỗi khi tạo vai trò: {}", e.getMessage());
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    @Transactional
    public RoleResponse update(String id, RoleRequest request) {
        log.info("Cập nhật vai trò có id: {}", id);

        // Kiểm tra đầu vào
        validateRoleRequest(request);

        var roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            log.error("Không tìm thấy vai trò với id: {}", id);
            throw new ResourceNotFoundEx(ErrorCode.ROLE_NOT_FOUND);
        }
        var role = roleOpt.get();

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        var permissionNames = request.getPermissions()
                .stream()
                .map(PermissionRequest::getName)
                .collect(Collectors.toList());
        var permissions = permissionRepository.findAllById(permissionNames);

        // Kiểm tra xem tất cả các quyền đều tồn tại
        if (permissions.size() != permissionNames.size()) {
            log.error("Một số quyền không tồn tại trong hệ thống");
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        role.setPermissions(new HashSet<>(permissions));

        try {
            role = roleRepository.save(role);
            log.info("Đã cập nhật vai trò thành công: {}", role.getName());
            return roleMapper.toRoleResponse(role);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật vai trò: {}", e.getMessage());
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


    @Transactional
    public void delete(String roleName) {
        log.info("Xóa vai trò: {}", roleName);

        // Kiểm tra xem vai trò có tồn tại không
        Optional<Role> roleOpt = roleRepository.findById(roleName);
        if (roleOpt.isEmpty()) {
            log.error("Không tìm thấy vai trò để xóa: {}", roleName);
            throw new ResourceNotFoundEx(ErrorCode.ROLE_NOT_FOUND);
        }

        try {
            roleRepository.deleteById(roleName);
            log.info("Đã xóa vai trò thành công: {}", roleName);
        } catch (DataIntegrityViolationException e) {
            log.error("Không thể xóa vai trò '{}' vì đang được sử dụng bởi người dùng", roleName);
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


    @Transactional
    public void assignRoleToUser(AssignRoleRequest request) {
        log.info("Gán vai trò '{}' cho người dùng có id '{}'", request.getRoleName(), request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.error("Không tìm thấy người dùng với id: {}", request.getUserId());
                    return new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED);
                });

        Role role = roleRepository.findById(request.getRoleName())
                .orElseThrow(() -> {
                    log.error("Không tìm thấy vai trò với tên: {}", request.getRoleName());
                    return new ResourceNotFoundEx(ErrorCode.ROLE_NOT_FOUND);
                });

        Set<Role> userRoles = user.getRoles();
        if (userRoles == null) {
            userRoles = new HashSet<>();
        }

        // Kiểm tra xem người dùng đã có vai trò này chưa
        if (userRoles.contains(role)) {
            log.info("Người dùng đã có vai trò này");
            return;
        }

        userRoles.add(role);
        user.setRoles(userRoles);

        try {
            userRepository.save(user);
            log.info("Đã gán vai trò '{}' cho người dùng '{}' thành công", role.getName(), user.getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi gán vai trò cho người dùng: {}", e.getMessage());
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    public void removeRoleFromUser(RemoveRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        Role role = roleRepository.findById(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.ROLE_NOT_FOUND));

        // Kiểm tra nếu user hiện có role này
        if (!user.getRoles().contains(role)) {
            throw new ResourceNotFoundEx(ErrorCode.USER_ROLES_NOT_FOUND);
        }

        // Kiểm tra nếu đây là role cuối cùng của user, không cho phép xóa
        if (user.getRoles().size() <= 1) {
            throw new ResourceNotFoundEx(ErrorCode.VALIDATION_ERROR);
        }

        // Xóa role
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    // Phương thức kiểm tra tính hợp lệ của dữ liệu đầu vào
    private void validateRoleRequest(RoleRequest request) {
        if (request == null) {
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        if (!StringUtils.hasText(request.getName())) {
            log.error("Tên vai trò không được để trống");
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
            log.error("Danh sách quyền không được để trống");
            throw new ResourceNotFoundEx(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


}
