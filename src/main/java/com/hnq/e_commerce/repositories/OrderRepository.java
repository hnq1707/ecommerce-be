package com.hnq.e_commerce.repositories;


import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUser(User user);
}