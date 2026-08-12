package likelion.mcmshowcase.metaverse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "avatar_preset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvatarPreset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    @Column(name = "model_asset_url", length = 1000)
    private String modelAssetUrl;
}
