package com.hnq.e_commerce.auth.repositories;

import com.hnq.e_commerce.auth.entities.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    List<InvalidatedToken> findByExpiryTimeBefore(Date expiryTime);

    // Xóa các token có thời gian hết hạn trước ngày thresholdDate
    void deleteByExpiryTimeBefore(Date expiryTime);
}
