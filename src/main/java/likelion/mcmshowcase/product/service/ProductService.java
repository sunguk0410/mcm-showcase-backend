package likelion.mcmshowcase.product.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.product.dto.ProductDetailResponse;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found: " + productId));

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getNameEn(),
                product.getPrice(),
                product.getColor(),
                product.getImageUrl()
        );
    }
}
