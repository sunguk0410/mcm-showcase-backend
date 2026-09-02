package likelion.mcmshowcase.product.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.global.exception.ErrorCode;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;

    @Test
    void productDetailIncludesKoreanAndEnglishNames() {
        Product product = product("한국어 상품명", "English Product Name");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var response = productService.getProductDetail(1L);

        assertEquals("한국어 상품명", response.name());
        assertEquals("English Product Name", response.nameEn());
    }

    @Test
    void productDetailAllowsNullEnglishName() {
        Product product = product("한국어 상품명", null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var response = productService.getProductDetail(1L);

        assertEquals("한국어 상품명", response.name());
        assertNull(response.nameEn());
    }

    @Test
    void productDetailThrowsCustomExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> productService.getProductDetail(99L)
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        assertEquals("Product not found: 99", exception.getDetail());
    }

    private Product product(String name, String nameEn) {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn(name);
        when(product.getNameEn()).thenReturn(nameEn);
        when(product.getPrice()).thenReturn(BigDecimal.valueOf(1_690_000));
        when(product.getColor()).thenReturn("Cognac");
        when(product.getImageUrl()).thenReturn("/images/products/1.png");
        return product;
    }
}
