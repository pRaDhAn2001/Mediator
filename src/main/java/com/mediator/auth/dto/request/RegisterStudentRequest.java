package com.mediator.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(name = "RegisterStudentRequest", description = "Information required to create a student account")
public class RegisterStudentRequest {
    @Schema(description = "Unique email address", example = "student@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Password (8–20 characters)", example = "Student@123", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    @Schema(description = "First name (minimum 3 characters)", example = "Rahul", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(min = 3, message = "First name must be at least 3 characters long")
    private String firstName;

    @Schema(description = "Last name (minimum 3 characters)", example = "Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(min = 3, message = "Last name must be at least 3 characters long")
    private String lastName;

    @Schema(description = "10-digit Indian mobile number", example = "9876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    public RegisterStudentRequest() {
    }

    public RegisterStudentRequest(String email, String password, String firstName, String lastName,
            String mobileNumber) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNumber = mobileNumber;
    }
}
