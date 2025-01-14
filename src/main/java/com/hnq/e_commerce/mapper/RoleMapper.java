package com.hnq.e_commerce.mapper;

import com.hnq.e_commerce.auth.dto.request.RoleRequest;
import com.hnq.e_commerce.auth.dto.response.RoleResponse;
import com.hnq.e_commerce.auth.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);


    Role dtoToRole(RoleResponse response);
}