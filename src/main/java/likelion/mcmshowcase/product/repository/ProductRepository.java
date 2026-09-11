package likelion.mcmshowcase.product.repository;

import java.util.List;
import likelion.mcmshowcase.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
            select p from Product p
            where (:category is null or p.category.code = :category)
              and (:gender is null or p.gender = :gender or p.gender = 'UNISEX')
              and (:zone is null or p.zone = :zone)
              and (:keyword is null
                   or lower(p.name) like lower(concat('%', :keyword, '%'))
                   or lower(p.nameEn) like lower(concat('%', :keyword, '%'))
                   or lower(p.productCode) like lower(concat('%', :keyword, '%')))
            order by p.id asc
            """)
    List<Product> findByFilters(@Param("category") String category, @Param("gender") String gender, @Param("zone") String zone, @Param("keyword") String keyword);
}