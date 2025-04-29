package kz.findmyname284.springbootproject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.model.Product;
import kz.findmyname284.springbootproject.model.Supplier;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySupplier(Supplier supplier);
}
