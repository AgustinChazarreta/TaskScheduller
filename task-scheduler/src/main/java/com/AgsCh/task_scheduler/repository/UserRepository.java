package com.AgsCh.task_scheduler.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
