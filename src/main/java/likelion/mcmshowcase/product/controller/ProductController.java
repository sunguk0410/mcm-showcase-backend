package likelion.mcmshowcase.product.controller;

import likelion.mcmshowcase.global.response.ApiResponse;
import likelion.mcmshowcase.product.dto.ProductDetailResponse;
import likelion.mcmshowcase.product.service.ProductService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductDetail(productId)));
    }
}
