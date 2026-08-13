package likelion.mcmshowcase.member.service;

import likelion.mcmshowcase.member.dto.MemberLoginRequest;
import likelion.mcmshowcase.member.dto.MemberLoginResponse;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class MemberLoginService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MemberLoginResponse login(MemberLoginRequest request) {
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(this::unauthorized);

        String passwordHash = hash(request.password());
        if (!MessageDigest.isEqual(
                member.getPassword().getBytes(StandardCharsets.UTF_8),
                passwordHash.getBytes(StandardCharsets.UTF_8)
        )) {
            throw unauthorized();
        }
        return new MemberLoginResponse(member.getId(), member.getName());
    }

    private String hash(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials.");
    }
}
