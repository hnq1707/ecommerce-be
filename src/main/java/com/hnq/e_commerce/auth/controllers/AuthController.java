package com.hnq.e_commerce.auth.controllers;

import com.hnq.e_commerce.auth.dto.request.*;
import com.hnq.e_commerce.auth.dto.response.AuthenticationResponse;
import com.hnq.e_commerce.auth.dto.response.IntrospectResponse;
import com.hnq.e_commerce.auth.dto.response.UserResponse;
import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.auth.services.AuthenticationService;
import com.hnq.e_commerce.auth.services.UserService;
import com.hnq.e_commerce.dto.ApiResponse;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthenticationService authenticationService;
    UserService userService;

    @PostMapping("/register")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request,
                                                     HttpServletResponse response) throws ParseException, JOSEException
    {
        var result = authenticationService.authenticate(request, response);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/verify")
    ApiResponse<Void> verifyCode(@RequestBody VerifyRequest request) {
        userService.verifyCode(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/check-user")
    ApiResponse<UserResponse> checkUser(@RequestBody OAuthRegistrationRequest request) {
        return ApiResponse.<UserResponse>builder().result(authenticationService.verifyOrCreateUser(request)).build();
    }
}

