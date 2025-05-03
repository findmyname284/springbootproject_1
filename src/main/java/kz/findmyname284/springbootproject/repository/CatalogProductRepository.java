package kz.findmyname284.springbootproject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.model.CatalogProduct;
import kz.findmyname284.springbootproject.model.Supplier;
import kz.findmyname284.springbootproject.model.WarehouseProduct;

public interface CatalogProductRepository extends JpaRepository<CatalogProduct, Long> {

    List<CatalogProduct> findAllBySeller(Supplier seller);

    CatalogProduct findBySellerAndBaseProduct(Supplier seller, WarehouseProduct baseProduct);
    
}
