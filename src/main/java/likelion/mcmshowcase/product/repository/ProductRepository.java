package likelion.mcmshowcase.product.repository;

import likelion.mcmshowcase.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
