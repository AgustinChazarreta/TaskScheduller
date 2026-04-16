package com.AgsCh.task_scheduler.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Role;
import com.AgsCh.task_scheduler.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);

    boolean existsByRole(Role role);

    long countByRole(Role role);

    List<User> findByHouseIdAndRole(Long houseId, Role role);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndHouse(String username, House house);

    boolean existsByUsernameIgnoreCase(String email);

    Optional<User> findByUsernameAndHouseId(String username, Long long1);

    // Todos los admins con AdminData cargado
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.adminData WHERE u.role = 'ADMIN'")
    List<User> findAllAdminsWithAdminData();

    // Todos los usuarios con AdminData cargado (opcional, si querés mostrar todos)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.adminData")
    List<User> findAllWithAdminData();

    // Administradores de una casa específica con AdminData
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.adminData WHERE u.role = 'ADMIN' AND u.house.id = :houseId")
    List<User> findAdminsByHouseIdWithAdminData(@Param("houseId") Long houseId);

    long countByRoleAndActiveTrue(Role webmaster);

    @Query("""
            SELECT u FROM User u
            JOIN FETCH u.adminData
            WHERE u.username = :username
            """)
    Optional<User> findByUsernameWithAdminData(String username);
}
