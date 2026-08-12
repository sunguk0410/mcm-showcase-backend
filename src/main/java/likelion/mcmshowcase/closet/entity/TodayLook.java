package likelion.mcmshowcase.closet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "today_look")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayLook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "style_profile_id", nullable = false)
    private StyleProfile styleProfile;
    @Column(name = "name", length = 255)
    private String name;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
