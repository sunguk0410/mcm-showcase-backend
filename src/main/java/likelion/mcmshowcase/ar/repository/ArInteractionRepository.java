package likelion.mcmshowcase.ar.repository;

import likelion.mcmshowcase.ar.entity.ArInteraction;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.entity.ArInteractionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArInteractionRepository extends JpaRepository<ArInteraction, Long> {
    Optional<ArInteraction> findTopByArSessionOrderBySequenceNoDesc(ArSession arSession);

    List<ArInteraction> findByArSessionOrderBySequenceNoAsc(ArSession arSession);

    List<ArInteraction> findByArSessionAndInteractionTypeOrderBySequenceNoAsc(
            ArSession arSession,
            ArInteractionType interactionType
    );
}
