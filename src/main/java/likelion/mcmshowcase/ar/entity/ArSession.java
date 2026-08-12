package likelion.mcmshowcase.ar.entity;

import jakarta.persistence.*;
import likelion.mcmshowcase.global.enums.Gender;
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_session_id", nullable = false)
    private CustomerSession customerSession;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static ArSession create(
            CustomerSession customerSession,
            Gender gender,
            LocalDateTime createdAt
    ) {
        ArSession arSession = new ArSession();
        arSession.customerSession = customerSession;
        arSession.gender = gender;
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
}
