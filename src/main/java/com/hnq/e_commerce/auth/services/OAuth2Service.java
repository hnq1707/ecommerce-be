package com.hnq.e_commerce.auth.services;

import com.hnq.e_commerce.auth.dto.response.RoleResponse;
import com.hnq.e_commerce.auth.entities.Role;
import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OAuth2Service {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    public Optional<User> getUser(String userName) {
        return userRepository.findByEmail(userName);
    }

    public User createUser(OAuth2User oAuth2User, String provider) {
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required for user creation");
        }

        List<RoleResponse> rolesList = roleService.getAll();
        if (rolesList == null || rolesList.isEmpty()) {
            throw new IllegalStateException("No roles available to assign to the user");
        }
        Set<Role> roles = rolesList.stream()
                .map(roleMapper::dtoToRole)
                .collect(Collectors.toSet());

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .provider(provider)
                .enabled(true)
                .roles(roles)
                .build();

        return userRepository.save(user);
    }


}