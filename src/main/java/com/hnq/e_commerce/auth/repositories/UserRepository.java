package com.hnq.e_commerce.auth.repositories;

import com.hnq.e_commerce.auth.entities.Role;
import com.hnq.e_commerce.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    List<User> findByRoles(Role role);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRolesName(@Param("roleName") String roleName);

    // Tìm tất cả admin (có vai trò ADMIN)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ADMIN' AND u.enabled = true")
    List<User> findAllAdminUsers();

    // Tìm tất cả người dùng có vai trò khác với vai trò được chỉ định
    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT u2.id FROM User u2 JOIN u2.roles r WHERE r.name = :roleName)")
    List<User> findByRolesNameNot(@Param("roleName") String roleName);

    // Tìm tất cả người dùng có một trong các vai trò được chỉ định
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    List<User> findByRolesNameIn(@Param("roleNames") List<String> roleNames);

    // Tìm người dùng theo vai trò và trạng thái kích hoạt
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.enabled = :enabled")
    List<User> findByRolesNameAndEnabled(@Param("roleName") String roleName, @Param("enabled") boolean enabled);

    // Tìm người dùng không hoạt động trong một khoảng thời gian
    @Query("SELECT u FROM User u WHERE u.updatedOn < :date")
    List<User> findByUpdatedOnBefore(@Param("date") Date date);

    // Tìm người dùng mới đăng ký trong khoảng thời gian
    @Query("SELECT u FROM User u WHERE u.createdOn BETWEEN :startDate AND :endDate")
    List<User> findByCreatedOnBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // Đếm số lượng người dùng theo vai trò
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    // Tìm người dùng theo tên hoặc email (cho tìm kiếm)
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}
