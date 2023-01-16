package com.adissu.reserve.service;

import com.adissu.reserve.constants.ResultConstants;
import com.adissu.reserve.dto.ProductDTO;
import com.adissu.reserve.entity.Product;
import com.adissu.reserve.repository.ProductRepository;
import com.adissu.reserve.util.ProductUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final List<Product> productList;

    public String insertNewProduct(final ProductDTO productDTO) {
        log.info("Validating product: {}", productDTO);
        if( productDTO.getProductName() == null || productDTO.getProductName().isBlank() || productDTO.getDurationInMinutes() == 0) {
            log.info("Product is invalid.");
            return ResultConstants.ERROR_INVALID;
        }

        log.info("Product is valid. Trying to insert now..");

        Product product = Product.builder()
                .productName(productDTO.getProductName())
                .durationInMinutes(productDTO.getDurationInMinutes())
                .build();

        productRepository.save(product);
        productList.add(product);

        log.info("Added product with name {}; duration {}; id {}; to the list.", product.getProductName(), product.getDurationInMinutes(), product.getId());

        return ResultConstants.SUCCESS;
    }

    public String insertNewProduct(String name, final String duration) {
        name = StringUtils.capitalize(name.toLowerCase());

        ProductDTO productDTO = ProductDTO.builder()
                .productName(name)
                .durationInMinutes(ProductUtil.formatDurationFromAdminInput(duration))
                .build();

        return insertNewProduct(productDTO);
    }

    public String modifyExistingProduct(final String id, final String duration, String name) {
        Optional<Product> productOptional = productRepository.findById(Integer.parseInt(id));

        if( productOptional.isEmpty() ) {
            log.info("Could not find product with id = {}", id);
            return ResultConstants.ERROR_NOT_FOUND;
        }

        Product product = productOptional.get();

        if( !duration.equals("nothing") ) {
            product.setDurationInMinutes(ProductUtil.formatDurationFromAdminInput(duration));
            log.info("modifyExistingProduct - new duration = {}", product.getDurationInMinutes());
        }

        if( name != null && !name.isBlank() ) {
            name = StringUtils.capitalize(name.toLowerCase());
            product.setProductName(name);
            log.info("modifyExistingProduct - new name = {}", product.getProductName());
        }

        productRepository.save(product);
        Product productFromList = productList.stream()
                .filter(product1 -> product1.getId() == Integer.parseInt(id))
                .findFirst()
                .get();

        productFromList.setProductName(product.getProductName());
        productFromList.setDurationInMinutes(product.getDurationInMinutes());

        log.info("modifyExistingProduct - modified the product");

        return ResultConstants.SUCCESS;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
