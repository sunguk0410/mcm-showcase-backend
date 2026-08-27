package likelion.mcmshowcase.visit.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.global.entity.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "zone_interaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZoneInteraction extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_session_id", nullable = false)
    private CustomerSession customerSession;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_category_id", nullable = false)
    private ZoneCategory zoneCategory;
    @Column(name = "entered_at", nullable = false)
    private LocalDateTime enteredAt;
    @Column(name = "exited_at")
    private LocalDateTime exitedAt;
    @Column(name = "dwell_seconds")
    private Integer dwellSeconds;
    public static ZoneInteraction create(
            CustomerSession customerSession,
            ZoneCategory zoneCategory,
            LocalDateTime enteredAt,
            LocalDateTime exitedAt,
            Integer dwellSeconds
    ) {
        ZoneInteraction interaction = new ZoneInteraction();
        interaction.customerSession = customerSession;
        interaction.zoneCategory = zoneCategory;
        interaction.enteredAt = enteredAt;
        interaction.exitedAt = exitedAt;
        interaction.dwellSeconds = dwellSeconds;
        interaction.initializeAuditTimestamps(LocalDateTime.now());
        return interaction;
    }
}
