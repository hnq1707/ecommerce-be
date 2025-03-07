package com.hnq.e_commerce.auth.dto.response;

import com.hnq.e_commerce.auth.entities.Role;
import com.hnq.e_commerce.entities.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private String phoneNumber;
    private String email;
    private Set<Role> roles;
    private List<Address> addressList;
}