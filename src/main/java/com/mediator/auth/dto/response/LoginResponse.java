package com.mediator.auth.dto.response;

import java.time.LocalDateTime;

import com.mediator.auth.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private final String tokenType = "Bearer";
    private String email;
    private Role role;
    private LocalDateTime expiresAt;
}
