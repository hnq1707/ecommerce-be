package com.hnq.e_commerce.repositories;

import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.entities.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findByUser(User user, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.address = NULL WHERE o.address.id = :addressId")
    void updateAddressToNull(@Param("addressId") String addressId);
}
