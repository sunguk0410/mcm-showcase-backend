package likelion.mcmshowcase.visit.repository;

import likelion.mcmshowcase.visit.entity.ZoneInteraction;
import likelion.mcmshowcase.visit.entity.CustomerSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneInteractionRepository extends JpaRepository<ZoneInteraction, Long> {
    List<ZoneInteraction> findByCustomerSessionOrderByEnteredAtAsc(CustomerSession customerSession);
}
