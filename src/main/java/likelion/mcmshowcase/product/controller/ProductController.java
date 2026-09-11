package likelion.mcmshowcase.product.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import likelion.mcmshowcase.product.dto.ProductDetailResponse;
import likelion.mcmshowcase.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import likelion.mcmshowcase.global.enums.Gender;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDetailResponse>>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProducts(category, gender, zone, keyword)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductDetail(productId)));
    }
}
