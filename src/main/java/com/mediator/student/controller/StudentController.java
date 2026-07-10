package com.mediator.student.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mediator.student.dto.dashboard.StudentDashboardResponse;
import com.mediator.student.dto.request.StudentProfileRequest;
import com.mediator.student.dto.response.StudentProfileResponse;
import com.mediator.student.service.StudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    /**
     * Returns logged-in student's profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> getProfile() {

        return ResponseEntity.ok(
                studentService.getProfile());
    }

    /**
     * Updates student profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            @Valid @RequestBody StudentProfileRequest request) {

        return ResponseEntity.ok(
                studentService.updateProfile(request));
    }

    /**
     * Student dashboard.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                studentService.getDashboard());
    }

}