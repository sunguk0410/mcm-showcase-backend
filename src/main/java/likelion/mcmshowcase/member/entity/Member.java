package likelion.mcmshowcase.member.entity;

import jakarta.persistence.*;
import likelion.mcmshowcase.global.enums.Gender;
import likelion.mcmshowcase.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
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
}
