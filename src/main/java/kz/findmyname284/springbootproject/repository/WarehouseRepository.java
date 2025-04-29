package kz.findmyname284.springbootproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
}
