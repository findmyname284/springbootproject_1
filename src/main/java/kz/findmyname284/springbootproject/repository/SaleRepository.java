package kz.findmyname284.springbootproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.model.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

}
