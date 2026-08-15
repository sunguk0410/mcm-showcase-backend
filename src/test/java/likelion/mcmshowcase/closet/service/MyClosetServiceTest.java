package likelion.mcmshowcase.closet.service;

import likelion.mcmshowcase.ar.entity.ArSession;
import likelion.mcmshowcase.ar.repository.ArInteractionRepository;
import likelion.mcmshowcase.ar.repository.ArSessionRepository;
import likelion.mcmshowcase.closet.dto.MyClosetMemberLinkRequest;
import likelion.mcmshowcase.closet.entity.StyleProfile;
import likelion.mcmshowcase.closet.repository.StyleProfileRepository;
import likelion.mcmshowcase.closet.repository.TodayLookItemRepository;
import likelion.mcmshowcase.closet.repository.TodayLookRepository;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyClosetServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock StyleProfileRepository styleProfileRepository;
    @Mock TodayLookRepository todayLookRepository;
    @Mock TodayLookItemRepository todayLookItemRepository;
    @Mock ArInteractionRepository arInteractionRepository;
    @Mock ArSessionRepository arSessionRepository;
    @InjectMocks MyClosetService myClosetService;

    @Test
    void anonymousUserCanReadStyleProfileDetail() {
        StyleProfile styleProfile = styleProfile(7L, arSession(34L));
        when(styleProfileRepository.findById(7L)).thenReturn(Optional.of(styleProfile));
        when(todayLookRepository.findTopByStyleProfileOrderByCreatedAtDesc(styleProfile))
                .thenReturn(Optional.empty());
        when(arInteractionRepository.findByArSessionAndInteractionTypeOrderBySequenceNoAsc(
                any(), any())).thenReturn(List.of());

        var response = myClosetService.getMyClosetDetail(7L);

        assertEquals(7L, response.styleProfileId());
        assertEquals("Signature", response.styleIdentityTitle());
    }

    @Test
    void linksMemberToUnownedStyleProfile() {
        ArSession arSession = arSession(34L);
        StyleProfile styleProfile = styleProfile(7L, arSession);
        Member member = member(1L);
        when(styleProfileRepository.findById(7L)).thenReturn(Optional.of(styleProfile));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        var response = myClosetService.linkMember(7L, new MyClosetMemberLinkRequest(1L));

        assertEquals(1L, response.memberId());
        assertSame(member, arSession.getMember());
        verify(arSessionRepository).save(arSession);
    }

    @Test
    void linkingSameMemberIsIdempotent() {
        ArSession arSession = arSession(34L);
        Member member = member(1L);
        arSession.mapMember(member);
        when(styleProfileRepository.findById(7L))
                .thenReturn(Optional.of(styleProfile(7L, arSession)));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        var response = myClosetService.linkMember(7L, new MyClosetMemberLinkRequest(1L));

        assertEquals(1L, response.memberId());
        verify(arSessionRepository).save(arSession);
    }

    @Test
    void linkingDifferentMemberReturnsConflict() {
        ArSession arSession = arSession(34L);
        arSession.mapMember(member(2L));
        Member requestedMember = member(1L);
        when(styleProfileRepository.findById(7L))
                .thenReturn(Optional.of(styleProfile(7L, arSession)));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(requestedMember));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> myClosetService.linkMember(7L, new MyClosetMemberLinkRequest(1L))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(arSessionRepository, never()).save(any());
    }

    @Test
    void missingStyleProfileReturnsNotFound() {
        when(styleProfileRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> myClosetService.linkMember(99L, new MyClosetMemberLinkRequest(1L))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void missingMemberReturnsNotFound() {
        when(styleProfileRepository.findById(7L))
                .thenReturn(Optional.of(styleProfile(7L, arSession(34L))));
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> myClosetService.linkMember(7L, new MyClosetMemberLinkRequest(99L))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void linkedStyleProfileAppearsInMemberCloset() {
        Member member = member(1L);
        StyleProfile styleProfile = styleProfile(7L, arSession(34L));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(styleProfileRepository.findByArSessionMemberOrderByCreatedAtDesc(member))
                .thenReturn(List.of(styleProfile));

        var response = myClosetService.getMyCloset(1L);

        assertEquals(List.of(7L), response.items().stream()
                .map(item -> item.styleProfileId())
                .toList());
    }

    private StyleProfile styleProfile(Long id, ArSession arSession) {
        StyleProfile styleProfile = StyleProfile.create(
                arSession,
                "Signature",
                "/images/avatars/female.png",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(styleProfile, "id", id);
        return styleProfile;
    }

    private ArSession arSession(Long id) {
        ArSession arSession = ArSession.create(LocalDateTime.now());
        ReflectionTestUtils.setField(arSession, "id", id);
        return arSession;
    }

    private Member member(Long id) {
        Member member = mock(Member.class);
        lenient().when(member.getId()).thenReturn(id);
        return member;
    }
}
