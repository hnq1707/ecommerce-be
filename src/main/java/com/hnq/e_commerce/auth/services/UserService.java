package com.hnq.e_commerce.auth.services;

import com.hnq.e_commerce.auth.constant.PredefinedRole;
import com.hnq.e_commerce.auth.dto.request.UserCreationRequest;
import com.hnq.e_commerce.auth.dto.request.UserUpdateRequest;
import com.hnq.e_commerce.auth.dto.request.VerifyRequest;
import com.hnq.e_commerce.auth.dto.response.UserResponse;
import com.hnq.e_commerce.auth.entities.Role;
import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.auth.helper.VerificationCodeGenerator;
import com.hnq.e_commerce.auth.repositories.RoleRepository;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.mapper.UserMapper;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setCreatedOn(new Date());
        user.setRoles(roles);
        user.setEnabled(false);
        user.setProvider("manual");

        String code = VerificationCodeGenerator.generateCode();

        user.setVerificationCode(code);
        emailService.sendVerificationEmail(user);


        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceNotFoundEx(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByEmail(name).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        user.setUpdatedOn(new Date());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void disableUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        user.setEnabled(false);
        userRepository.save(user);
        log.info("Tài khoản người dùng {} đã bị vô hiệu hóa", user.getEmail());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

//    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED)));
    }
    @Transactional
    public void verifyCode(@NotNull VerifyRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        if (!user.getVerificationCode().equals(code)) {
            throw new ResourceNotFoundEx(ErrorCode.INVALID_KEY);
        }

        user.setEnabled(true);
        user.setUpdatedOn(new Date());
        userRepository.save(user);
    }


    public void renewVerificationCode(String email) {
        String code = VerificationCodeGenerator.generateCode();

        String input = email.replaceAll(".*\"email\":\"([^\"]+)\".*", "$1");

       Optional<User> userOptional = userRepository.findByEmail(input);
       if (userOptional.isEmpty()) {
           throw new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED);
       }
        User user = userOptional.get();
        user.setVerificationCode(code);
        userRepository.save(user);
        emailService.sendVerificationEmail(user);
    }

    public List<String> getAllAdminIds() {
        Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.ROLE_NOT_FOUND));

        return userRepository.findByRoles(adminRole).stream()
                .map(User::getId)
                .toList();
    }

}