package likelion.mcmshowcase.visit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_zone")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreZone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    @Column(name = "floor_code", length = 20, nullable = false)
    private String floorCode;
}
