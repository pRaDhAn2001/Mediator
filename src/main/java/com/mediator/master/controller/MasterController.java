package com.mediator.master.controller;

import com.mediator.common.dto.ApiResponse;
import com.mediator.master.dto.BoardResponse;
import com.mediator.master.dto.ClassLevelResponse;
import com.mediator.master.dto.SubjectResponse;
import com.mediator.master.service.MasterService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master")
@RequiredArgsConstructor
@Tag(name = "Master Data", description = "APIs for retrieving master data such as boards, class levels and subjects")
public class MasterController {

    private final MasterService masterService;

    @GetMapping("/boards")
    @Operation(summary = "Get all boards", description = "Retrieves the list of available educational boards.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Boards fetched successfully")
    })
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getBoards() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Boards fetched successfully.",
                        masterService.getAllBoards()));
    }

    @GetMapping("/class-levels")
    @Operation(summary = "Get all class levels", description = "Retrieves the list of available class levels.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class levels fetched successfully")
    })
    public ResponseEntity<ApiResponse<List<ClassLevelResponse>>> getClassLevels() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Class levels fetched successfully.",
                        masterService.getAllClassLevels()));
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get all subjects", description = "Retrieves the list of available academic subjects.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subjects fetched successfully")
    })
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjects() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subjects fetched successfully.",
                        masterService.getAllSubjects()));
    }
}