package kz.findmyname284.springbootproject.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kz.findmyname284.springbootproject.model.Product;
import kz.findmyname284.springbootproject.model.Supplier;
import kz.findmyname284.springbootproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    
    public List<Product> findBySupplier(Supplier supplier) {
        return productRepository.findBySupplier(supplier);
    }

    public void newProduct(Product product) {
        productRepository.save(product);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public void save(Product product) {
        productRepository.save(product);
    }
}
