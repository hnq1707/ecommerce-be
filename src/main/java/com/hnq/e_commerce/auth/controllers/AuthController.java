package com.hnq.e_commerce.auth.controllers;

import com.hnq.e_commerce.auth.dto.request.*;
import com.hnq.e_commerce.auth.dto.response.AuthenticationResponse;
import com.hnq.e_commerce.auth.dto.response.IntrospectResponse;
import com.hnq.e_commerce.auth.dto.response.UserResponse;
import com.hnq.e_commerce.auth.services.AuthenticationService;
import com.hnq.e_commerce.auth.services.UserService;
import com.hnq.e_commerce.dto.response.ApiResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;


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
            throws ParseException, JOSEException
    {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException
    {
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

    @PostMapping("/renew")
    ApiResponse<?> renewVerificationCode(@RequestBody String email) {
        userService.renewVerificationCode(email);
        return ApiResponse.builder().build();
    }

    @PostMapping("/check-user")
    ApiResponse<AuthenticationResponse> checkUser(@RequestBody OAuthRegistrationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder().result(authenticationService.verifyOrCreateUser(request)).build();
    }
    @PostMapping("/forget-password")
    ApiResponse<Void> forgetPassword(@RequestBody ForgetPasswordRequest request) {
        authenticationService.forgetPassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.<Void>builder().message("Reset password successfully").build();
    }
}

