package likelion.mcmshowcase.visit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private CustomerSessionStatus status;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static CustomerSession createAnonymous(LocalDateTime createdAt) {
        CustomerSession customerSession = new CustomerSession();
        customerSession.status = CustomerSessionStatus.ACTIVE;
        customerSession.startedAt = createdAt;
        customerSession.endedAt = null;
        customerSession.createdAt = createdAt;
        return customerSession;
    }

    public void end(LocalDateTime endedAt) {
        if (endedAt.isBefore(this.startedAt)) {
            throw new IllegalArgumentException("endedAt cannot be earlier than startedAt");
        }
        this.status = CustomerSessionStatus.COMPLETED;
        this.endedAt = endedAt;
    }
}
