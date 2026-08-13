package likelion.mcmshowcase.member.entity;

import jakarta.persistence.*;
import likelion.mcmshowcase.global.enums.Gender;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "external_member_id", length = 100, nullable = false, unique = true)
    private String externalMemberId;
    @Column(name = "login_id", length = 100, nullable = false, unique = true)
    private String loginId;
    @Column(name = "password", length = 64, nullable = false)
    private String password;
    @Column(name = "name", length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
