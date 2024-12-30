package com.hnq.e_commerce.auth.repositories;


import com.hnq.e_commerce.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailRepository extends JpaRepository<User, Long> {
    User findByEmail(String username);
}