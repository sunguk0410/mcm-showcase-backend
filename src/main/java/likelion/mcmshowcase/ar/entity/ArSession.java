package likelion.mcmshowcase.ar.entity;

import jakarta.persistence.*;
import likelion.mcmshowcase.global.enums.Gender;
import likelion.mcmshowcase.member.entity.Member;
import lombok.*;
import likelion.mcmshowcase.visit.entity.CustomerSession;
import java.time.LocalDateTime;

@Entity
@Table(name = "ar_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_session_id")
    private CustomerSession customerSession;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static ArSession create(LocalDateTime createdAt) {
        ArSession arSession = new ArSession();
        arSession.customerSession = null;
        arSession.member = null;
        arSession.gender = null;
        arSession.startedAt = createdAt;
        arSession.endedAt = null;
        arSession.createdAt = createdAt;
        return arSession;
    }

    public void end(LocalDateTime endedAt) {
        if (this.endedAt != null) {
            throw new IllegalStateException("ArSession is already ended");
        }
        if (endedAt.isBefore(this.startedAt)) {
            throw new IllegalArgumentException("endedAt cannot be earlier than startedAt");
        }
        this.endedAt = endedAt;
    }

    public void mapCustomerSession(CustomerSession customerSession) {
        if (this.customerSession != null && !this.customerSession.getId().equals(customerSession.getId())) {
            throw new IllegalStateException("ArSession is already mapped to another CustomerSession");
        }
        this.customerSession = customerSession;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void mapMember(Member member) {
        if (this.member != null && !this.member.getId().equals(member.getId())) {
            throw new IllegalStateException("ArSession is already mapped to another Member");
        }
        this.member = member;
    }
}
