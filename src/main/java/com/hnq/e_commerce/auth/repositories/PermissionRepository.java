package com.hnq.e_commerce.auth.repositories;

import com.hnq.e_commerce.auth.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
