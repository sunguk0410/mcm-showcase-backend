package likelion.mcmshowcase.member.controller;

import jakarta.validation.Valid;
import likelion.mcmshowcase.member.dto.MemberLoginRequest;
import likelion.mcmshowcase.member.dto.MemberLoginResponse;
import likelion.mcmshowcase.member.service.MemberLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberLoginService memberLoginService;

    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponse> login(
            @Valid @RequestBody MemberLoginRequest request
    ) {
        return ResponseEntity.ok(memberLoginService.login(request));
    }
}
