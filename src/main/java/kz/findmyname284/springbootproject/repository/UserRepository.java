package kz.findmyname284.springbootproject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.enums.UserRole;
import kz.findmyname284.springbootproject.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    boolean existsByPhone(String phone);
}
