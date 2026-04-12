package com.capstone.user.controller;

import com.capstone.user.dto.MeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        String email = authentication.getName();

        return ResponseEntity.ok(new MeResponse(
                1L,
                email,
                "yj",
                "/uploads/profile/1.png"
        ));
    }
}