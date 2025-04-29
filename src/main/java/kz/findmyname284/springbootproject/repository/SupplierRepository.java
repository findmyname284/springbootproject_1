package kz.findmyname284.springbootproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.model.Supplier;
import kz.findmyname284.springbootproject.model.User;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUser(User user);

    Supplier findByUser(User user);
    
}
