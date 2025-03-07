package com.hnq.e_commerce.services;

import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.dto.AddressRequest;
import com.hnq.e_commerce.entities.Address;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.repositories.AddressRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AddressService {


    AddressRepository addressRepository;
    private final UserRepository userRepository;

    public Address createAddress(AddressRequest addressRequest, Principal principal) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = jwt.getClaim("sub");
        User user =
                userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        Address address = Address.builder()
                .name(addressRequest.getName())
                .street(addressRequest.getStreet())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .zipCode(addressRequest.getZipCode())
                .phoneNumber(addressRequest.getPhoneNumber())
                .user(user)
                .build();

        return addressRepository.save(address);
    }


    public void deleteAddress(UUID id) {
        addressRepository.deleteById(id);
    }
}
