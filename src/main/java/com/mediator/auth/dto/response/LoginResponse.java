package com.mediator.auth.dto.response;

import com.mediator.auth.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(name = "LoginResponse", description = "Authentication result and issued JWT")
public class LoginResponse {
    @Schema(description = "Signed JWT access token")
    private String token;

    @Schema(description = "Authorization token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "User identifier", example = "101")
    private Long userId;

    @Schema(description = "User email", example = "student@example.com")
    private String email;

    @Schema(description = "First name", example = "Rahul")
    private String firstName;

    @Schema(description = "Last name", example = "Sharma")
    private String lastName;

    @Schema(description = "Application role", example = "ROLE_STUDENT")
    private Role role;

    @Schema(description = "Token validity duration in seconds", example = "86400")
    private Long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, String tokenType, Long userId, String email,
            String firstName, String lastName, Role role, Long expiresIn) {
        this.token = token;
        this.tokenType = tokenType;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}
