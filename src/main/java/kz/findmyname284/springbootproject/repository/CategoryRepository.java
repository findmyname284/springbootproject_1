package kz.findmyname284.springbootproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.findmyname284.springbootproject.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
}
