package com.adissu.reserve.repository;

import com.adissu.reserve.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    public Product findByProductName(String productName);
}
