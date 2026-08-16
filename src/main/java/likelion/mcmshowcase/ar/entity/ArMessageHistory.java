package likelion.mcmshowcase.ar.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ar_message_history", indexes = {
        @Index(name = "idx_ar_message_history_session", columnList = "ar_session_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArMessageHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ar_session_id", nullable = false)
    private ArSession arSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", length = 40, nullable = false)
    private MessageTriggerType triggerType;

    @Column(name = "zone", length = 30)
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_level", length = 20)
    private InterestLevel interestLevel;

    @Column(name = "target_category", length = 100)
    private String targetCategory;

    @Column(name = "message_id", length = 50, nullable = false)
    private String messageId;

    @Column(name = "message", length = 500, nullable = false)
    private String message;

    @Column(name = "fitting_sequence_no", nullable = false)
    private Integer fittingSequenceNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static ArMessageHistory create(ArSession arSession, MessageTriggerType triggerType,
            String zone, InterestLevel interestLevel, String targetCategory,
            String messageId, String message, int fittingSequenceNo, LocalDateTime createdAt) {
        ArMessageHistory history = new ArMessageHistory();
        history.arSession = arSession;
        history.triggerType = triggerType;
        history.zone = zone;
        history.interestLevel = interestLevel;
        history.targetCategory = targetCategory;
        history.messageId = messageId;
        history.message = message;
        history.fittingSequenceNo = fittingSequenceNo;
        history.createdAt = createdAt;
        return history;
    }
}
