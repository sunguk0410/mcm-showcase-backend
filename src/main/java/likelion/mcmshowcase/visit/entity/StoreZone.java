package likelion.mcmshowcase.visit.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.global.entity.BaseEntity;

@Entity
@Table(name = "store_zone")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreZone extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    @Column(name = "floor_code", length = 20, nullable = false)
    private String floorCode;
}
