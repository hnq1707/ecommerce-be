package com.hnq.e_commerce.mapper;

import com.hnq.e_commerce.auth.dto.request.PermissionRequest;
import com.hnq.e_commerce.auth.dto.response.PermissionResponse;
import com.hnq.e_commerce.auth.entities.Permission;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}