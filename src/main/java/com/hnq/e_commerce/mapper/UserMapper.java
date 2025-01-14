package com.hnq.e_commerce.mapper;

import com.hnq.e_commerce.auth.dto.request.UserUpdateRequest;
import com.hnq.e_commerce.auth.dto.request.UserCreationRequest;
import com.hnq.e_commerce.auth.dto.response.UserResponse;
import com.hnq.e_commerce.auth.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "addressList", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
