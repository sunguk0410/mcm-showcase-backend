package likelion.mcmshowcase.metaverse.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import java.time.LocalDateTime;

@Entity
@Table(name = "avatar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Avatar {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "style_profile_id", nullable = false)
    private StyleProfile styleProfile;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avatar_preset_id", nullable = false)
    private AvatarPreset avatarPreset;
    @Column(name = "preview_image_url", length = 1000)
    private String previewImageUrl;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
