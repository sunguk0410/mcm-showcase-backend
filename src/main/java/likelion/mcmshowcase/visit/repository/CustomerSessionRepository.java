package likelion.mcmshowcase.visit.repository;

import likelion.mcmshowcase.visit.entity.CustomerSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSessionRepository extends JpaRepository<CustomerSession, Long> {
}
