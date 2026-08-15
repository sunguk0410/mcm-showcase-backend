package likelion.mcmshowcase.closet.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.ar.entity.ArSession;
import java.time.LocalDateTime;

@Entity
@Table(name = "style_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StyleProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ar_session_id", nullable = false)
    private ArSession arSession;
    @Column(name = "style_identity_title", length = 255, nullable = false)
    private String styleIdentityTitle;
    @Column(name = "avatar_image_url", length = 500, nullable = false)
    private String avatarImageUrl;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static StyleProfile create(
            ArSession arSession,
            String styleIdentityTitle,
            String avatarImageUrl,
            LocalDateTime createdAt
    ) {
        StyleProfile styleProfile = new StyleProfile();
        styleProfile.arSession = arSession;
        styleProfile.styleIdentityTitle = styleIdentityTitle;
        styleProfile.avatarImageUrl = avatarImageUrl;
        styleProfile.createdAt = createdAt;
        return styleProfile;
    }

    public void updateAvatarImageUrl(String avatarImageUrl) {
        this.avatarImageUrl = avatarImageUrl;
    }
}
