package likelion.mcmshowcase.closet.repository;

import likelion.mcmshowcase.closet.entity.TodayLookItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayLookItemRepository extends JpaRepository<TodayLookItem, Long> {
}
