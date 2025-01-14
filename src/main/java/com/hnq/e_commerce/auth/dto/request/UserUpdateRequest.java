package com.hnq.e_commerce.auth.dto.request;


import com.hnq.e_commerce.entities.Address;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstName;
    String lastName;
    String phoneNumber;
    String email;
    List<String> roles;
    List<String> addressList;
}
