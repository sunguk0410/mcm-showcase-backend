package likelion.mcmshowcase.visit.repository;

import likelion.mcmshowcase.product.entity.Category;
import likelion.mcmshowcase.visit.entity.StoreZone;
import likelion.mcmshowcase.visit.entity.ZoneCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZoneCategoryRepository extends JpaRepository<ZoneCategory, Long> {
    Optional<ZoneCategory> findByZoneAndCategory(StoreZone zone, Category category);
}
