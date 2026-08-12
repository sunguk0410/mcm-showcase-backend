package likelion.mcmshowcase.visit.entity;

import jakarta.persistence.*;
import lombok.*;
import likelion.mcmshowcase.member.entity.Member;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private CustomerSessionStatus status;
    @Column(name = "identified_at")
    private LocalDateTime identifiedAt;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static CustomerSession createAnonymous(LocalDateTime createdAt) {
        CustomerSession customerSession = new CustomerSession();
        customerSession.member = null;
        customerSession.status = CustomerSessionStatus.ACTIVE;
        customerSession.identifiedAt = null;
        customerSession.startedAt = createdAt;
        customerSession.endedAt = null;
        customerSession.createdAt = createdAt;
        return customerSession;
    }

    public void identifyMember(Member member, LocalDateTime identifiedAt) {
        this.member = member;
        this.identifiedAt = identifiedAt;
    }
}
