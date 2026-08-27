package likelion.mcmshowcase.closet.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.global.entity.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "today_look")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayLook extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "style_profile_id", nullable = false)
    private StyleProfile styleProfile;
    public static TodayLook create(StyleProfile styleProfile, LocalDateTime createdAt) {
        TodayLook todayLook = new TodayLook();
        todayLook.styleProfile = styleProfile;
        todayLook.initializeAuditTimestamps(createdAt);
        return todayLook;
    }
}
