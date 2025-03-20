package com.hnq.e_commerce.auth.repositories;

import com.hnq.e_commerce.auth.entities.Role;
import com.hnq.e_commerce.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    List<User> findByRoles(Role role);
}
