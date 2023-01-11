package com.adissu.reserve.config;

import com.adissu.reserve.entity.AdminConfig;
import com.adissu.reserve.entity.Product;
import com.adissu.reserve.repository.AdminConfigRepository;
import com.adissu.reserve.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final AdminConfigRepository adminConfigRepository;
    private final ProductRepository productRepository;

    @Bean
    public List<AdminConfig> adminConfigList() {
        return adminConfigRepository.findAll();
    }

    @Bean
    public List<Product> productList() {
        return productRepository.findAll();
    }
}
