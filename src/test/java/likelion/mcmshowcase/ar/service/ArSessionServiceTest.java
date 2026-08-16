package likelion.mcmshowcase.ar.service;

import likelion.mcmshowcase.ar.dto.ArSessionMemberRequest;
import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.repository.MemberRepository;
import likelion.mcmshowcase.recommendation.service.RecommendationService;
import likelion.mcmshowcase.visit.repository.CustomerSessionRepository;
import likelion.mcmshowcase.visit.repository.ZoneInteractionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArSessionServiceTest {

    @Mock CustomerSessionRepository customerSessionRepository;
    @Mock ArSessionRepository arSessionRepository;
    @Mock ArInteractionRepository arInteractionRepository;
    @Mock ZoneInteractionRepository zoneInteractionRepository;
    @Mock RecommendationService recommendationService;
    @Mock MemberRepository memberRepository;
    @InjectMocks ArSessionService arSessionService;

    @Test
    void existingArSessionMemberPatchStillLinksMember() {
        ArSession arSession = ArSession.create(LocalDateTime.now());
        ReflectionTestUtils.setField(arSession, "id", 34L);
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(1L);
        when(arSessionRepository.findById(34L)).thenReturn(Optional.of(arSession));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(arSessionRepository.save(arSession)).thenReturn(arSession);

        TransactionSynchronizationManager.initSynchronization();
        try {
            var response = arSessionService.mapMember(34L, new ArSessionMemberRequest(1L));

            assertEquals(34L, response.arSessionId());
            assertEquals(1L, response.memberId());
            verify(arSessionRepository).save(arSession);
            verifyNoInteractions(recommendationService);

            TransactionSynchronizationManager.getSynchronizations().forEach(
                    TransactionSynchronization::afterCommit);
            verify(recommendationService).initializePreferences(34L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
