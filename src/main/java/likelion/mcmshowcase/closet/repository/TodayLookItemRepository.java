package likelion.mcmshowcase.closet.repository;

import likelion.mcmshowcase.closet.entity.TodayLookItem;
import likelion.mcmshowcase.closet.entity.TodayLook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodayLookItemRepository extends JpaRepository<TodayLookItem, Long> {
    List<TodayLookItem> findByTodayLookOrderByDisplayOrderAsc(TodayLook todayLook);
}
