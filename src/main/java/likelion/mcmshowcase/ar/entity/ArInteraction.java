package likelion.mcmshowcase.ar.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.product.entity.Product;
import java.time.LocalDateTime;

@Entity
@Table(name = "ar_interaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArInteraction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ar_session_id", nullable = false)
    private ArSession arSession;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", length = 40, nullable = false)
    private ArInteractionType interactionType;
    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static ArInteraction create(
            ArSession arSession,
            Product product,
            ArInteractionType interactionType,
            int sequenceNo,
            LocalDateTime createdAt
    ) {
        ArInteraction arInteraction = new ArInteraction();
        arInteraction.arSession = arSession;
        arInteraction.product = product;
        arInteraction.interactionType = interactionType;
        arInteraction.sequenceNo = sequenceNo;
        arInteraction.createdAt = createdAt;
        return arInteraction;
    }
}
