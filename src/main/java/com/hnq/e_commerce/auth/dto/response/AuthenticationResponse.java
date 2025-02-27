package com.hnq.e_commerce.auth.dto.response;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String id;
    String email;
    String accessToken;
    List<String> roles;
    boolean authenticated;
}