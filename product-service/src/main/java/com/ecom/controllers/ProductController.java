package com.ecom.controllers;

import com.ecom.dto.ProductCatalogueRequest;
import com.ecom.dto.StockUpdateRequest;
import com.ecom.entities.Product;
import com.ecom.dto.ApiResponse;
import com.ecom.dto.ProductResponse;
import com.ecom.services.ProductService;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Value("${message}")
    private String message;

    private Logger logger = LoggerFactory.getLogger(ProductController.class);

    //create
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/saveProduct")
    public ResponseEntity<ProductResponse> createUser(@RequestBody ProductCatalogueRequest productCatalogueRequest) {
        ProductResponse productResponseDto = productService.saveProduct(productCatalogueRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productResponseDto);
    }

    //single user get

    @GetMapping("/{userId}")
    public ResponseEntity<ProductResponse> getSingleUser(@PathVariable Long userId) {
        logger.info("Get Single User Handler: UserController");

        ProductResponse user = productService.getProduct(userId);
        return ResponseEntity.ok(user);
    }


    //all user get
    @PermitAll
    @GetMapping("/getAllProducts")
    public ResponseEntity<List<Product>> getAllUser() {
        logger.info("message::"+message);

        List<Product> allUser = productService.getAllProducts();
        if(allUser!=null)
            logger.info("Number of users found: {}", allUser.size());
        else
            logger.info("No users found.");
        return ResponseEntity.ok(allUser);
    }
//    @PutMapping("/admin/{id}")
//    public ResponseEntity<ProductResponse> updateProduct(
//            @PathVariable Long id,
//            @Validated @RequestBody ProductCatalogueRequest request) {
//
//        return ResponseEntity.ok(
//                productService.updateProduct(id, request)
//        );
//    }

    @PutMapping("/update")
    public ResponseEntity<ProductResponse> updateProductClient(
            @Validated @RequestBody StockUpdateRequest request) {

        return ResponseEntity.ok(
                productService.updateProduct(
                        request.getProductId(),
                        request.getQuantity())
        );
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
