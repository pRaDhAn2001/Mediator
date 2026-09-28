package com.mediator.master.controller;

import com.mediator.common.exception.GlobalExceptionHandler;
import com.mediator.master.dto.BoardResponse;
import com.mediator.master.dto.ClassLevelResponse;
import com.mediator.master.dto.SubjectResponse;
import com.mediator.master.service.MasterService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mediator.auth.config.SecurityConfig;
import com.mediator.auth.security.JwtAuthenticationFilter;

import com.mediator.common.exception.ResourceNotFoundException;

@WebMvcTest(controllers = MasterController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class,
        JwtAuthenticationFilter.class
}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("MasterController WebMvc Tests")
class MasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MasterService masterService;

    // =========================================================
    // BOARDS
    // =========================================================

    @Nested
    @DisplayName("GET /api/v1/master/boards")
    class GetBoardsTests {

        @Test
        @DisplayName("Should return HTTP 200 with boards")
        void getBoards_success() throws Exception {

            BoardResponse board1 = BoardResponse.builder()
                    .id(1L)
                    .name("CBSE")
                    .build();

            BoardResponse board2 = BoardResponse.builder()
                    .id(2L)
                    .name("ICSE")
                    .build();

            when(masterService.getAllBoards())
                    .thenReturn(List.of(board1, board2));

            mockMvc.perform(
                    get("/api/v1/master/boards")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.message")
                            .value("Boards fetched successfully."))

                    .andExpect(jsonPath("$.data")
                            .isArray())

                    .andExpect(jsonPath("$.data.length()")
                            .value(2))

                    .andExpect(jsonPath("$.data[0].id")
                            .value(1))

                    .andExpect(jsonPath("$.data[0].name")
                            .value("CBSE"))

                    .andExpect(jsonPath("$.data[1].id")
                            .value(2))

                    .andExpect(jsonPath("$.data[1].name")
                            .value("ICSE"))

                    .andExpect(jsonPath("$.timestamp")
                            .exists());

            verify(masterService).getAllBoards();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should return HTTP 200 with empty board list")
        void getBoards_emptyList() throws Exception {

            when(masterService.getAllBoards())
                    .thenReturn(List.of());

            mockMvc.perform(
                    get("/api/v1/master/boards")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.message")
                            .value("Boards fetched successfully."))

                    .andExpect(jsonPath("$.data")
                            .isArray())

                    .andExpect(jsonPath("$.data.length()")
                            .value(0))

                    .andExpect(jsonPath("$.timestamp")
                            .exists());

            verify(masterService).getAllBoards();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should return error when master service cannot fetch boards")
        void getBoards_serviceException() throws Exception {

            when(masterService.getAllBoards())
                    .thenThrow(
                            new ResourceNotFoundException(
                                    "No boards found."));

            mockMvc.perform(
                    get("/api/v1/master/boards")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success")
                            .value(false))

                    .andExpect(jsonPath("$.message")
                            .value("No boards found."));

            verify(masterService).getAllBoards();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should reject POST request")
        void getBoards_wrongHttpMethod() throws Exception {

            mockMvc.perform(
                    post("/api/v1/master/boards")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());

            verifyNoInteractions(masterService);
        }
    }

    // =========================================================
    // CLASS LEVELS
    // =========================================================

    @Nested
    @DisplayName("GET /api/v1/master/class-levels")
    class GetClassLevelsTests {

        @Test
        @DisplayName("Should return HTTP 200 with class levels")
        void getClassLevels_success() throws Exception {

            ClassLevelResponse classLevel1 = ClassLevelResponse.builder()
                    .id(1L)
                    .standard(9)
                    .build();

            ClassLevelResponse classLevel2 = ClassLevelResponse.builder()
                    .id(2L)
                    .standard(10)
                    .build();

            when(masterService.getAllClassLevels())
                    .thenReturn(
                            List.of(
                                    classLevel1,
                                    classLevel2));

            mockMvc.perform(
                    get("/api/v1/master/class-levels")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.message")
                            .value("Class levels fetched successfully."))

                    .andExpect(jsonPath("$.data")
                            .isArray())

                    .andExpect(jsonPath("$.data.length()")
                            .value(2))

                    .andExpect(jsonPath("$.data[0].id")
                            .value(1))

                    .andExpect(jsonPath("$.data[0].standard")
                            .value(9))

                    .andExpect(jsonPath("$.data[1].id")
                            .value(2))

                    .andExpect(jsonPath("$.data[1].standard")
                            .value(10))

                    .andExpect(jsonPath("$.timestamp")
                            .exists());

            verify(masterService).getAllClassLevels();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should return HTTP 200 with empty class-level list")
        void getClassLevels_emptyList() throws Exception {

            when(masterService.getAllClassLevels())
                    .thenReturn(List.of());

            mockMvc.perform(
                    get("/api/v1/master/class-levels")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.message")
                            .value("Class levels fetched successfully."))

                    .andExpect(jsonPath("$.data")
                            .isArray())

                    .andExpect(jsonPath("$.data.length()")
                            .value(0))

                    .andExpect(jsonPath("$.timestamp")
                            .exists());

            verify(masterService).getAllClassLevels();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should return error when master service cannot fetch class levels")
        void getClassLevels_serviceException() throws Exception {

            when(masterService.getAllClassLevels())
                    .thenThrow(
                            new ResourceNotFoundException(
                                    "No class levels found."));

            mockMvc.perform(
                    get("/api/v1/master/class-levels")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success")
                            .value(false))

                    .andExpect(jsonPath("$.message")
                            .value("No class levels found."));

            verify(masterService).getAllClassLevels();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should reject POST request")
        void getClassLevels_wrongHttpMethod() throws Exception {

            mockMvc.perform(
                    post("/api/v1/master/class-levels")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());

            verifyNoInteractions(masterService);
        }
    }

    // =========================================================
    // SUBJECTS
    // =========================================================

    @Nested
    @DisplayName("GET /api/v1/master/subjects")
    class GetSubjectsTests {

        @Test
        @DisplayName("Should return HTTP 200 with subjects")
        void getSubjects_success() throws Exception {

            SubjectResponse subject1 = SubjectResponse.builder()
                    .id(1L)
                    .name("Mathematics")
                    .build();

            SubjectResponse subject2 = SubjectResponse.builder()
                    .id(2L)
                    .name("Physics")
                    .build();

            SubjectResponse subject3 = SubjectResponse.builder()
                    .id(3L)
                    .name("Chemistry")
                    .build();

            when(masterService.getAllSubjects())
                    .thenReturn(
                            List.of(
                                    subject1,
                                    subject2,
                                    subject3));

            mockMvc.perform(
                    get("/api/v1/master/subjects")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.message")
                            .value("Subjects fetched successfully."))

                    .andExpect(jsonPath("$.data")
                            .isArray())

                    .andExpect(jsonPath("$.data.length()")
                            .value(3))

                    .andExpect(jsonPath("$.data[0].id")
                            .value(1))

                    .andExpect(jsonPath("$.data[0].name")
                            .value("Mathematics"))

                    .andExpect(jsonPath("$.data[1].id")
                            .value(2))

                    .andExpect(jsonPath("$.data[1].name")
                            .value("Physics"))

                    .andExpect(jsonPath("$.data[2].id")
                            .value(3))

                    .andExpect(jsonPath("$.data[2].name")
                            .value("Chemistry"))

                    .andExpect(jsonPath("$.timestamp")
                            .exists());

            verify(masterService).getAllSubjects();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should return HTTP 200 with empty subject list")
        void getSubjects_emptyList() throws Exception {

            when(masterService.getAllSubjects())
                    .thenReturn(List.of());

            mockMvc.perform(
                    get("/api/v1/master/subjects")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.message")
                            .value("Subjects fetched successfully."))

                    .andExpect(jsonPath("$.data")
                            .isArray())

                    .andExpect(jsonPath("$.data.length()")
                            .value(0))

                    .andExpect(jsonPath("$.timestamp")
                            .exists());

            verify(masterService).getAllSubjects();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should return error when master service cannot fetch subjects")
        void getSubjects_serviceException() throws Exception {

            when(masterService.getAllSubjects())
                    .thenThrow(
                            new ResourceNotFoundException(
                                    "No subjects found."));

            mockMvc.perform(
                    get("/api/v1/master/subjects")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success")
                            .value(false))

                    .andExpect(jsonPath("$.message")
                            .value("No subjects found."));

            verify(masterService).getAllSubjects();
            verifyNoMoreInteractions(masterService);
        }

        @Test
        @DisplayName("Should reject POST request")
        void getSubjects_wrongHttpMethod() throws Exception {

            mockMvc.perform(
                    post("/api/v1/master/subjects")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());

            verifyNoInteractions(masterService);
        }
    }
}