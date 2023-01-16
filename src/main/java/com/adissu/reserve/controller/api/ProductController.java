package com.adissu.reserve.controller.api;

import com.adissu.reserve.constants.ResultConstants;
import com.adissu.reserve.dto.ProductDTO;
import com.adissu.reserve.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/api/product/")
public class ProductController {

    private final ProductService productService;

    @PostMapping(path = "new")
    @RolesAllowed("admin")
    public ResponseEntity<String> insertNewProduct(@RequestBody ProductDTO productDTO) {
        if( productService.insertNewProduct(productDTO).equals(ResultConstants.ERROR_INVALID) ) {
            return ResponseEntity.badRequest().body("Product is invalid.");
        }

        return ResponseEntity.ok("Product has been saved!");
    }
}
