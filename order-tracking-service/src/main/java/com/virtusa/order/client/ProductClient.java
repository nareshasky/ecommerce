package com.virtusa.order.client;

import com.virtusa.order.dto.ProductResponseDto;
import com.virtusa.order.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "PRODUCT-SERVICE",
        path = "/products",
        fallback = ProductClientFallback.class
)
public interface ProductClient {

    @GetMapping("/{productId}")
    ProductResponseDto getProduct(@PathVariable("productId") Long productId);

    @PutMapping("/update")
    ProductResponseDto reduceStock(@RequestBody StockUpdateRequest request);
}
