package com.travelagency.web;

import com.travelagency.dto.ProductBookingRequest;
import com.travelagency.dto.ProductSearchRequest;
import com.travelagency.dto.TravelProductResponse;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    public List<TravelProductResponse> search(@ModelAttribute ProductSearchRequest req) {
        return productService.search(req);
    }

    @GetMapping("/{id}")
    public TravelProductResponse one(@PathVariable Long id) {
        return productService.get(id);
    }

    @PostMapping("/bookings")
    public Map<String, Long> book(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ProductBookingRequest req) {
        return Map.of("id", productService.book(principal, req));
    }
}
