package com.mediator.master.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.master.repository.BoardRepository;
import com.mediator.master.repository.ClassLevelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.repository.TutorRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentTutorIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SubjectRepository subjectRepository;

  @Autowired
  private BoardRepository boardRepository;

  @Autowired
  private ClassLevelRepository classLevelRepository;

  @Autowired
  private TutorRepository tutorRepository;

  private String registerAndGetToken(
      String endpoint,
      String requestBody) throws Exception {

    MvcResult result = mockMvc.perform(post(endpoint)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token").isNotEmpty())
        .andReturn();

    JsonNode response = objectMapper.readTree(
        result.getResponse().getContentAsString());

    return response.path("data").path("token").asText();
  }

  private String registerStudent() throws Exception {
    String suffix = String.valueOf(System.nanoTime());
    String email = "student." + suffix + "@example.com";
    String phone = "9" + suffix.substring(
        Math.max(0, suffix.length() - 9));

    return registerAndGetToken(
        "/api/v1/auth/register/student",
        """
            {
              "email": "%s",
              "password": "Test@12345",
              "firstName": "Test",
              "lastName": "Student",
              "mobileNumber": "%s"
            }
            """.formatted(email, phone));
  }

  private String registerTutor() throws Exception {
    String suffix = String.valueOf(System.nanoTime());
    String email = "tutor." + suffix + "@example.com";
    String phone = "9" + suffix.substring(
        Math.max(0, suffix.length() - 9));

    return registerAndGetToken(
        "/api/v1/auth/register/tutor",
        """
            {
              "email": "%s",
              "password": "Test@12345",
              "firstName": "Test",
              "lastName": "Tutor",
              "mobileNumber": "%s"
            }
            """.formatted(email, phone));
  }

  private String bearer(String token) {
    return "Bearer " + token;
  }

  @Test
  void studentCanRegisterViewDashboardAndManagePreferences()
      throws Exception {

    String token = registerStudent();

    // Fetch the profile created during registration.
    mockMvc.perform(get("/api/v1/students/profile")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").isNotEmpty());

    // Fetch the dashboard.
    mockMvc.perform(get("/api/v1/students/dashboard")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isNotEmpty());

    Long subjectId = subjectRepository.findByName("Mathematics")
        .orElseThrow(() -> new AssertionError("Mathematics was not seeded"))
        .getId();

    // Save preferred subjects.
    mockMvc.perform(put("/api/v1/students/preferences")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "subjectIds": [%d]
            }
            """.formatted(subjectId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // Verify that the preference was persisted.
    mockMvc.perform(get("/api/v1/students/preferences")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(
            subjectId));
  }

  @Test
  void tutorCanRegisterViewDashboardAndManageTeachingPreferences()
      throws Exception {

    String token = registerTutor();

    mockMvc.perform(get("/api/v1/tutors/profile")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").isNotEmpty());

    mockMvc.perform(get("/api/v1/tutors/dashboard")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isNotEmpty());

    Long subjectId = subjectRepository.findByName("Mathematics")
        .orElseThrow()
        .getId();

    Long boardId = boardRepository.findByName("CBSE")
        .orElseThrow()
        .getId();

    Long classLevelId = classLevelRepository.findByStandard(10)
        .orElseThrow()
        .getId();

    // Save one teaching preference.
    mockMvc.perform(post("/api/v1/tutors/preferences")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "preferences": [
                {
                  "subjectId": %d,
                  "classLevelId": %d,
                  "boardId": %d
                }
              ]
            }
            """.formatted(
            subjectId, classLevelId, boardId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // Verify the saved preference.
    mockMvc.perform(get("/api/v1/tutors/preferences")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  void tutorCanUploadRetrieveAndDeleteDocument()
      throws Exception {

    String token = registerTutor();

    // Upload a document reference; no physical file is uploaded here.
    mockMvc.perform(post("/api/v1/tutors/documents")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "documents": [
                {
                  "documentType": "CLASS_10_MARKSHEET",
                  "documentUrl": "https://example.com/test-marksheet.pdf"
                }
              ]
            }
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true));

    MvcResult result = mockMvc.perform(
        get("/api/v1/tutors/documents")
            .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andReturn();

    JsonNode response = objectMapper.readTree(
        result.getResponse().getContentAsString());

    long documentId = response.path("data")
        .get(0).path("documentId").asLong();

    assertTrue(documentId > 0);

    mockMvc.perform(delete(
        "/api/v1/tutors/documents/{documentId}",
        documentId)
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    mockMvc.perform(get("/api/v1/tutors/documents")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void protectedStudentAndTutorEndpointsRejectUnauthenticatedRequests()
      throws Exception {

    mockMvc.perform(get("/api/v1/students/profile"))
        .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/api/v1/tutors/profile"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void studentPreferenceValidationRejectsEmptySubjectList()
      throws Exception {

    String token = registerStudent();

    mockMvc.perform(put("/api/v1/students/preferences")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "subjectIds": []
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message")
            .value("Validation failed."));
  }

  @Test
  void studentCanUpdateProfileAndRetrieveUpdatedValues() throws Exception {
    String token = registerStudent();

    Long boardId = boardRepository.findByName("CBSE")
        .orElseThrow()
        .getId();

    Long classLevelId = classLevelRepository.findByStandard(8)
        .orElseThrow()
        .getId();

    String request = """
        {
          "gender": "MALE",
          "address": {
            "street": "10 Park Street",
            "city": "Kolkata",
            "district": "Kolkata",
            "state": "West Bengal",
            "zipCode": "700016",
            "latitude": 22.5535,
            "longitude": 88.3517
          },
          "preferredMode": {
            "online": true,
            "tutorHome": false,
            "studentHome": true
          },
          "preferredRadius": 10.0,
          "classLevelId": %d,
          "boardId": %d,
          "schoolName": "Test School",
          "parentName": "Test Parent",
          "parentPhone": "9876543210",
          "parentEmail": "parent@example.com",
          "sameAsPrimaryPhone": false,
          "sameAsPrimaryEmail": false
        }
        """.formatted(classLevelId, boardId);

    mockMvc.perform(put("/api/v1/students/profile")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.gender").value("MALE"))
        .andExpect(jsonPath("$.data.schoolName").value("Test School"))
        .andExpect(jsonPath("$.data.parentName").value("Test Parent"))
        .andExpect(jsonPath("$.data.classLevelId").value(classLevelId))
        .andExpect(jsonPath("$.data.boardId").value(boardId));

    mockMvc.perform(get("/api/v1/students/profile")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.schoolName").value("Test School"))
        .andExpect(jsonPath("$.data.parentEmail").value("parent@example.com"));
  }

  @Test
  void studentProfileUpdateWithInvalidRequestReturnsBadRequest()
      throws Exception {
    String token = registerStudent();

    mockMvc.perform(put("/api/v1/students/profile")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "gender": null,
              "parentName": ""
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed."))
        .andExpect(jsonPath("$.errors.gender").exists())
        .andExpect(jsonPath("$.errors.address").exists())
        .andExpect(jsonPath("$.errors.preferredMode").exists());
  }

  @Test
  void studentProfileUpdateWithUnknownBoardReturnsNotFound()
      throws Exception {
    String token = registerStudent();

    Long classLevelId = classLevelRepository.findByStandard(8)
        .orElseThrow()
        .getId();

    mockMvc.perform(put("/api/v1/students/profile")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "gender": "MALE",
              "address": {
                "city": "Kolkata"
              },
              "preferredMode": {
                "online": true,
                "tutorHome": false,
                "studentHome": false
              },
              "classLevelId": %d,
              "boardId": 9223372036854775807,
              "parentName": "Test Parent",
              "parentPhone": "9876543210",
              "parentEmail": "parent@example.com"
            }
            """.formatted(classLevelId)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void studentPreferenceWithUnknownSubjectReturnsNotFound()
      throws Exception {
    String token = registerStudent();

    mockMvc.perform(put("/api/v1/students/preferences")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "subjectIds": [9223372036854775807]
            }
            """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message")
            .value("One or more selected subjects do not exist."));
  }

  @Test
  void tutorProfileUpdateWithInvalidRequestReturnsBadRequest()
      throws Exception {
    String token = registerTutor();

    mockMvc.perform(put("/api/v1/tutors/profile")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "gender": null,
              "description": "",
              "address": null,
              "academicProfile": null,
              "teachingExperienceYears": -1,
              "preferredMode": null,
              "salaryMin": -100,
              "salaryMax": 500
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed."))
        .andExpect(jsonPath("$.errors.gender").exists())
        .andExpect(jsonPath("$.errors.description").exists())
        .andExpect(jsonPath("$.errors.academicProfile").exists());
  }

  @Test
  void tutorTeachingPreferencesWithUnknownSubjectReturnsNotFound()
      throws Exception {
    String token = registerTutor();

    Long boardId = boardRepository.findByName("CBSE")
        .orElseThrow()
        .getId();

    Long classLevelId = classLevelRepository.findByStandard(10)
        .orElseThrow()
        .getId();

    mockMvc.perform(post("/api/v1/tutors/preferences")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "preferences": [
                {
                  "subjectId": 9223372036854775807,
                  "classLevelId": %d,
                  "boardId": %d
                }
              ]
            }
            """.formatted(classLevelId, boardId)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Subject not found."));
  }

  @Test
  void tutorTeachingPreferencesWithEmptyListReturnsBadRequest()
      throws Exception {
    String token = registerTutor();

    mockMvc.perform(post("/api/v1/tutors/preferences")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "preferences": []
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed."));
  }

  @Test
  void tutorCannotUploadDuplicateDocumentTypesInOneRequest()
      throws Exception {
    String token = registerTutor();

    mockMvc.perform(post("/api/v1/tutors/documents")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "documents": [
                {
                  "documentType": "CLASS_10_MARKSHEET",
                  "documentUrl": "https://example.com/marksheet1.pdf"
                },
                {
                  "documentType": "CLASS_10_MARKSHEET",
                  "documentUrl": "https://example.com/marksheet2.pdf"
                }
              ]
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message")
            .value("Duplicate document type in request: CLASS_10_MARKSHEET"));
  }

  @Test
  void tutorCannotUploadEmptyDocumentList() throws Exception {
    String token = registerTutor();

    mockMvc.perform(post("/api/v1/tutors/documents")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "documents": []
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed."));
  }

  @Test
  void tutorCannotDeleteNonexistentDocument() throws Exception {
    String token = registerTutor();

    mockMvc.perform(delete("/api/v1/tutors/documents/{documentId}",
        9223372036854775807L)
        .header("Authorization", bearer(token)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Document not found."));
  }

  @Test
  void registeringStudentWithDuplicateEmailReturnsConflict()
      throws Exception {
    String suffix = String.valueOf(System.nanoTime());
    String email = "duplicate.student." + suffix + "@example.com";
    String phone = "9" + suffix.substring(Math.max(0, suffix.length() - 9));

    String request = """
        {
          "email": "%s",
          "password": "Test@12345",
          "firstName": "Test",
          "lastName": "Student",
          "mobileNumber": "%s"
        }
        """.formatted(email, phone);

    mockMvc.perform(post("/api/v1/auth/register/student")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().is2xxSuccessful());

    mockMvc.perform(post("/api/v1/auth/register/student")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void tutorCanUpdateProfileAndRetrieveUpdatedValues() throws Exception {
    String token = registerTutor();

    String request = """
        {
          "gender": "MALE",
          "description": "Experienced tutor for Mathematics and Science",
          "address": {
            "street": "12 Main Road",
            "city": "Kolkata",
            "district": "Kolkata",
            "state": "West Bengal",
            "zipCode": "700001",
            "latitude": 22.5726,
            "longitude": 88.3639
          },
          "academicProfile": {
            "class10Percentage": 90.0,
            "class12Percentage": 88.0,
            "highestQualification": "BACHELORS",
            "degreeName": "B.Tech",
            "specialization": "Electronics",
            "instituteName": "Test University",
            "passingYear": 2023
          },
          "teachingExperienceYears": 2,
          "industryExperienceYears": 1,
          "currentOccupation": "Software Engineer",
          "preferredMode": {
            "online": true,
            "tutorHome": false,
            "studentHome": true
          },
          "preferredRadius": 10.0,
          "salaryMin": 3000,
          "salaryMax": 8000,
          "demoVideoUrl": "https://example.com/demo"
        }
        """;

    mockMvc.perform(put("/api/v1/tutors/profile")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.description")
            .value("Experienced tutor for Mathematics and Science"))
        .andExpect(jsonPath("$.data.teachingExperienceYears").value(2))
        .andExpect(jsonPath("$.data.currentOccupation")
            .value("Software Engineer"))
        .andExpect(jsonPath("$.data.preferredRadius").value(10.0));

    mockMvc.perform(get("/api/v1/tutors/profile")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.description")
            .value("Experienced tutor for Mathematics and Science"))
        .andExpect(jsonPath("$.data.salaryMin").value(3000))
        .andExpect(jsonPath("$.data.salaryMax").value(8000));
  }

  @Test
  void tutorProfileUpdateWithMinimumSalaryGreaterThanMaximumReturnsBadRequest()
      throws Exception {
    String token = registerTutor();

    String request = """
        {
          "gender": "MALE",
          "description": "Test tutor",
          "address": {
            "street": "12 Main Road",
            "city": "Kolkata",
            "district": "Kolkata",
            "state": "West Bengal",
            "zipCode": "700001",
            "latitude": 22.5726,
            "longitude": 88.3639
          },
          "academicProfile": {
            "class10Percentage": 90.0,
            "class12Percentage": 88.0,
            "highestQualification": "BACHELORS",
            "degreeName": "B.Tech",
            "specialization": "ECE",
            "instituteName": "Test University",
            "passingYear": 2023
          },
          "teachingExperienceYears": 2,
          "industryExperienceYears": 1,
          "currentOccupation": "Engineer",
          "preferredMode": {
            "online": true,
            "tutorHome": false,
            "studentHome": true
          },
          "preferredRadius": 10.0,
          "salaryMin": 9000,
          "salaryMax": 3000,
          "demoVideoUrl": "https://example.com/demo"
        }
        """;

    mockMvc.perform(put("/api/v1/tutors/profile")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  void studentCanLoginWithRegisteredCredentials() throws Exception {
    String suffix = String.valueOf(System.nanoTime());
    String email = "student." + suffix + "@example.com";
    String phone = "9" + suffix.substring(Math.max(0, suffix.length() - 9));
    String password = "Test@12345";

    String request = """
        {
          "email": "%s",
          "password": "%s",
          "firstName": "Test",
          "lastName": "Student",
          "mobileNumber": "%s"
        }
        """.formatted(email, password, phone);

    mockMvc.perform(post("/api/v1/auth/register/student")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().is2xxSuccessful());

    String loginRequest = """
        {
          "email": "%s",
          "password": "%s"
        }
        """.formatted(email, password);

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(loginRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token").isNotEmpty());
  }

  @Test
  void loginWithIncorrectPasswordReturnsUnauthorized() throws Exception {
    String suffix = String.valueOf(System.nanoTime());
    String email = "student." + suffix + "@example.com";
    String phone = "9" + suffix.substring(Math.max(0, suffix.length() - 9));

    String registration = """
        {
          "email": "%s",
          "password": "Correct@12345",
          "firstName": "Test",
          "lastName": "Student",
          "mobileNumber": "%s"
        }
        """.formatted(email, phone);

    mockMvc.perform(post("/api/v1/auth/register/student")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registration))
        .andExpect(status().is2xxSuccessful());

    String login = """
        {
          "email": "%s",
          "password": "Wrong@12345"
        }
        """.formatted(email);

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(login))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginWithUnknownEmailReturnsUnauthorized() throws Exception {
    String login = """
        {
          "email": "unknown.%s@example.com",
          "password": "Test@12345"
        }
        """.formatted(UUID.randomUUID());

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(login))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void protectedEndpointRejectsInvalidBearerToken() throws Exception {
    mockMvc.perform(get("/api/v1/students/profile")
        .header("Authorization", "Bearer invalid.jwt.token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void studentCannotAccessTutorEndpointsIfRolesAreEnforced() throws Exception {
    String studentToken = registerStudent();

    mockMvc.perform(get("/api/v1/tutors/profile")
        .header("Authorization", bearer(studentToken)))
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          assertTrue(status == 403 || status == 404,
              "Expected 403 or 404 but was: " + status);
        });
  }

  @Test
  void tutorCannotAccessStudentEndpointsIfRolesAreEnforced() throws Exception {
    String tutorToken = registerTutor();

    mockMvc.perform(get("/api/v1/students/profile")
        .header("Authorization", bearer(tutorToken)))
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          assertTrue(status == 403 || status == 404,
              "Expected 403 or 404 but was: " + status);
        });
  }

  @Test
  void subscriptionPlansContainExpectedPlanNamesAndPrices() throws Exception {
    String token = registerStudent();
    mockMvc.perform(get("/api/v1/subscriptions/plans")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath(
            "$.data[?(@.planType == 'BASIC' && @.price == 299)"
                + "]")
            .exists())
        .andExpect(jsonPath(
            "$.data[?(@.planType == 'PRO' && @.price == 1599)"
                + "]")
            .exists())
        .andExpect(jsonPath(
            "$.data[?(@.planType == 'ULTRA' && @.price == 2999)"
                + "]")
            .exists());
  }

  @Test
  void tutorSearchRequiresAuthentication() throws Exception {
    mockMvc.perform(post("/api/v1/search/tutors")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "subjectIds": [1]
            }
            """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void subscriptionPlansCanBeRetrievedWithoutAuthentication()
      throws Exception {

    mockMvc.perform(get("/api/v1/subscriptions/plans"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void studentCanSearchTutorsWithValidFilters() throws Exception {
    String studentToken = registerStudent();

    Long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow()
        .getId();

    String request = """
        {
          "subjectIds": [%d],
          "page": 0,
          "size": 10,
          "verifiedOnly": true
        }
        """.formatted(mathematicsId);

    mockMvc.perform(post("/api/v1/search/tutors")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.tutors").isArray())
        .andExpect(jsonPath("$.data.currentPage").value(0));
  }

  @Test
  void tutorSearchRejectsEmptySubjectIds() throws Exception {
    String studentToken = registerStudent();

    String request = """
        {
          "subjectIds": [],
          "page": 0,
          "size": 10
        }
        """;

    mockMvc.perform(post("/api/v1/search/tutors")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  void tutorSearchRejectsInvalidPageSize() throws Exception {
    String studentToken = registerStudent();

    Long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow()
        .getId();

    String request = """
        {
          "subjectIds": [%d],
          "page": 0,
          "size": 100
        }
        """.formatted(mathematicsId);

    mockMvc.perform(post("/api/v1/search/tutors")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isBadRequest());
  }

  @Test
  void tutorSearchRejectsUnauthenticatedRequest() throws Exception {
    Long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow()
        .getId();

    String request = """
        {
          "subjectIds": [%d],
          "page": 0,
          "size": 10
        }
        """.formatted(mathematicsId);

    mockMvc.perform(post("/api/v1/search/tutors")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void tutorSearchDoesNotExposeUnapprovedTutors() throws Exception {
    String studentToken = registerStudent();
    String tutorToken = registerTutor();

    Long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow()
        .getId();

    Long boardId = boardRepository.findByName("CBSE")
        .orElseThrow()
        .getId();

    Long classLevelId = classLevelRepository.findByStandard(10)
        .orElseThrow()
        .getId();

    String preferenceRequest = """
        {
          "preferences": [
            {
              "subjectId": %d,
              "classLevelId": %d,
              "boardId": %d
            }
          ]
        }
        """.formatted(mathematicsId, classLevelId, boardId);

    mockMvc.perform(post("/api/v1/tutors/preferences")
        .header("Authorization", bearer(tutorToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content(preferenceRequest))
        .andExpect(status().isOk());

    String searchRequest = """
        {
          "subjectIds": [%d],
          "classLevelId": %d,
          "boardId": %d,
          "page": 0,
          "size": 10
        }
        """.formatted(mathematicsId, classLevelId, boardId);

    mockMvc.perform(post("/api/v1/search/tutors")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content(searchRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.tutors").isArray())
        .andExpect(jsonPath(
            "$.data.tutors[?(@.verificationStatus == 'PENDING')]")
            .doesNotExist());
  }

  @Test
  void subscriptionPlansContainAllSeededPlans() throws Exception {
    String token = registerStudent();

    mockMvc.perform(get("/api/v1/subscriptions/plans")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath(
            "$.data[?(@.planType == 'BASIC')]").exists())
        .andExpect(jsonPath(
            "$.data[?(@.planType == 'PRO')]").exists())
        .andExpect(jsonPath(
            "$.data[?(@.planType == 'ULTRA')]").exists());
  }

  @Test
  void studentRequestsEndpointRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/matches/student"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void tutorRequestsEndpointRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/matches/tutor"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void matchRequestDetailsRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/matches/1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void cancelMatchRequestRequiresAuthentication() throws Exception {
    mockMvc.perform(put("/api/v1/matches/1/cancel"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void connectMatchRequestRequiresAuthentication() throws Exception {
    mockMvc.perform(put("/api/v1/matches/1/connect"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void finalizeMatchRequestRequiresAuthentication() throws Exception {
    mockMvc.perform(put("/api/v1/matches/1/finalize"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void partialFinalizeMatchRequestRequiresAuthentication() throws Exception {
    mockMvc.perform(put("/api/v1/matches/1/partial-finalize")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "acceptedSubjectIds": [1]
            }
            """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void studentCanRetrieveOwnMatchRequests() throws Exception {
    String studentToken = registerStudent();

    mockMvc.perform(get("/api/v1/matches/student")
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void tutorCanRetrieveOwnMatchRequests() throws Exception {
    String tutorToken = registerTutor();

    mockMvc.perform(get("/api/v1/matches/tutor")
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void currentSubscriptionRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/subscriptions/current"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void subscriptionHistoryRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/subscriptions/history"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void matchRequestDetailsRejectsZeroId() throws Exception {
    String token = registerStudent();

    mockMvc.perform(get("/api/v1/matches/0")
        .header("Authorization", bearer(token)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void matchRequestDetailsRejectsNegativeId() throws Exception {
    String token = registerStudent();

    mockMvc.perform(get("/api/v1/matches/-1")
        .header("Authorization", bearer(token)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void matchRequestDetailsReturnsNotFoundForUnknownId() throws Exception {
    String token = registerStudent();

    mockMvc.perform(get("/api/v1/matches/999999999")
        .header("Authorization", bearer(token)))
        .andExpect(status().isNotFound());
  }

  @Test
  void cancelMatchRequestRejectsZeroId() throws Exception {
    String token = registerStudent();

    mockMvc.perform(put("/api/v1/matches/0/cancel")
        .header("Authorization", bearer(token)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void cancelMatchRequestRejectsNegativeId() throws Exception {
    String token = registerStudent();

    mockMvc.perform(put("/api/v1/matches/-1/cancel")
        .header("Authorization", bearer(token)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void cancelMatchRequestReturnsNotFoundForUnknownId() throws Exception {
    String token = registerStudent();

    mockMvc.perform(put("/api/v1/matches/999999999/cancel")
        .header("Authorization", bearer(token)))
        .andExpect(status().isNotFound());
  }

  @Test
  void tutorSearchRejectsMalformedJson() throws Exception {
    String token = registerStudent();

    mockMvc.perform(post("/api/v1/search/tutors")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{invalid-json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void tutorSearchRejectsEmptyRequestBody() throws Exception {
    String token = registerStudent();

    mockMvc.perform(post("/api/v1/search/tutors")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void tutorSearchRejectsInvalidSubjectIdsWhenValidated() throws Exception {
    String token = registerStudent();

    mockMvc.perform(post("/api/v1/search/tutors")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "subjectIds": [],
              "page": 0,
              "size": 10
            }
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void subscriptionPurchaseRequiresAuthentication() throws Exception {
    mockMvc.perform(post("/api/v1/subscriptions")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void currentSubscriptionReturnsNotFoundForNoActiveSubscription() throws Exception {
    String token = registerTutor();

    mockMvc.perform(get("/api/v1/subscriptions/current")
        .header("Authorization", bearer(token)))
        .andExpect(status().isNotFound());
  }

  @Test
  void subscriptionHistoryReturnsEmptyListForNewUser() throws Exception {
    String token = registerTutor();

    mockMvc.perform(get("/api/v1/subscriptions/history")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void createSubscriptionRejectsNonExistentPlanType() throws Exception {
    String token = registerTutor();

    mockMvc.perform(post("/api/v1/subscriptions")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("\"INVALID_PLAN\""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createSubscriptionRejectsZeroSubscriptionAmount() throws Exception {
    String token = registerTutor();

    mockMvc.perform(post("/api/v1/subscriptions")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createSubscriptionRejectsNegativeSubscriptionAmount() throws Exception {
    String token = registerTutor();

    mockMvc.perform(post("/api/v1/subscriptions")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content("-100"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createMatchRequestRejectsMissingTutorId() throws Exception {
    String studentToken = registerStudent();
    Long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow()
        .getId();

    mockMvc.perform(post("/api/v1/matches")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "subjectIds": [%d],
              "message": "Looking for a Mathematics tutor"
            }
            """.formatted(mathematicsId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createMatchRequestRejectsEmptySubjectIds() throws Exception {
    String studentToken = registerStudent();

    mockMvc.perform(post("/api/v1/matches")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "tutorId": 999999999,
              "subjectIds": [],
              "message": "Looking for a tutor"
            }
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createMatchRequestReturnsNotFoundForUnknownTutor() throws Exception {
    String studentToken = registerStudent();
    Long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow()
        .getId();

    mockMvc.perform(post("/api/v1/matches")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "tutorId": 999999999,
              "subjectIds": [%d],
              "message": "Looking for a Mathematics tutor"
            }
            """.formatted(mathematicsId)))
        .andExpect(status().isNotFound());
  }

  @Test
  void cancelMatchRequestRejectsUnauthenticatedRequestWithInvalidId() throws Exception {
    // Authentication is checked before path-level validation in this API.
    mockMvc.perform(put("/api/v1/matches/0/cancel"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void subscriptionPlansEndpointIsPublic() throws Exception {
    String token = registerStudent();

    mockMvc.perform(get("/api/v1/subscriptions/plans")
        .header("Authorization", bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void matchRequestRejectsZeroId() throws Exception {
    String studentToken = registerStudent();

    mockMvc.perform(get("/api/v1/matches/0")
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void matchRequestReturnsNotFoundForUnknownId() throws Exception {
    String studentToken = registerStudent();

    mockMvc.perform(get("/api/v1/matches/999999999")
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isNotFound());
  }

  /*
   * These helpers intentionally inspect the JSON response rather than
   * assuming whether the API calls an identifier "id", "tutorId", or
   * "requestId". This keeps the tests tied to the real HTTP contract
   * while allowing response DTOs to use their own identifier names.
   */
  private Long findLongField(JsonNode node, String... fieldNames) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }

    if (node.isObject()) {
      for (String fieldName : fieldNames) {
        JsonNode value = node.get(fieldName);
        if (value != null && value.canConvertToLong()) {
          return value.asLong();
        }
      }
      var fields = node.fields();
      while (fields.hasNext()) {
        Long found = findLongField(fields.next().getValue(), fieldNames);
        if (found != null) {
          return found;
        }
      }
    } else if (node.isArray()) {
      for (JsonNode child : node) {
        Long found = findLongField(child, fieldNames);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private String responseBody(MvcResult result) throws Exception {
    return result.getResponse().getContentAsString();
  }

  private long tutorIdFromProfile(String tutorToken) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/tutors/profile")
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode json = objectMapper.readTree(responseBody(result));
    Long id = findLongField(json, "tutorId", "id");
    assertNotNull(id, "Tutor profile response should contain a tutor identifier");
    return id;
  }

  private long createMatchRequest(
      String studentToken,
      long tutorId,
      long subjectId) throws Exception {

    MvcResult result = mockMvc.perform(post("/api/v1/matches")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "tutorId": %d,
              "subjectIds": [%d],
              "message": "Integration test match request"
            }
            """.formatted(tutorId, subjectId)))
        .andReturn();

    String body = responseBody(result);
    int status = result.getResponse().getStatus();

    assertTrue(status >= 200 && status < 300,
        "Create match failed. HTTP " + status
            + "; response body: " + body);

    JsonNode json = objectMapper.readTree(body);

    assertTrue(json.path("success").asBoolean(),
        "Expected success=true. Response: " + body);

    Long id = findLongField(json, "requestId", "matchRequestId", "id");

    assertNotNull(id,
        "Create-match response has no request identifier: " + body);

    return id;
  }

  private long subjectId(String subjectName) {
    return subjectRepository.findByName(subjectName)
        .orElseThrow(() -> new AssertionError(subjectName + " was not seeded"))
        .getId();
  }

  private long createMatchRequest(
      String studentToken,
      long tutorId,
      List<Long> subjectIds) throws Exception {

    String body = objectMapper.writeValueAsString(
        java.util.Map.of(
            "tutorId", tutorId,
            "subjectIds", subjectIds,
            "message", "Integration test match request"));

    MvcResult result = mockMvc.perform(post("/api/v1/matches")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andReturn();

    String response = responseBody(result);
    assertEquals(201, result.getResponse().getStatus(),
        "Create match should return 201. Response: " + response);

    JsonNode json = objectMapper.readTree(response);
    assertTrue(json.path("success").asBoolean(),
        "Expected success=true. Response: " + response);

    Long id = findLongField(json, "requestId", "matchRequestId", "id");
    assertNotNull(id, "Create response must contain requestId. Response: " + response);
    return id;
  }

  private void configureTutorForMatching(
      String tutorToken,
      long tutorId,
      List<Long> subjectIds) throws Exception {

    Tutor tutor = tutorRepository.findById(tutorId)
        .orElseThrow(() -> new AssertionError("Registered tutor was not found"));
    tutor.setVerificationStatus(VerificationStatus.APPROVED);
    tutorRepository.saveAndFlush(tutor);

    Long boardId = boardRepository.findByName("CBSE")
        .orElseThrow(() -> new AssertionError("CBSE board was not seeded"))
        .getId();
    Long classLevelId = classLevelRepository.findByStandard(10)
        .orElseThrow(() -> new AssertionError("Class 10 was not seeded"))
        .getId();

    String preferences = objectMapper.writeValueAsString(
        java.util.Map.of(
            "preferences",
            subjectIds.stream()
                .map(id -> java.util.Map.of(
                    "subjectId", id,
                    "classLevelId", classLevelId,
                    "boardId", boardId))
                .toList()));

    mockMvc.perform(post("/api/v1/tutors/preferences")
        .header("Authorization", bearer(tutorToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content(preferences))
        .andExpect(status().isOk());
  }

  private void configureStudentForMatching(String studentToken)
      throws Exception {

    Long boardId = boardRepository.findByName("CBSE")
        .orElseThrow(() -> new AssertionError("CBSE board was not seeded"))
        .getId();

    Long classLevelId = classLevelRepository.findByStandard(10)
        .orElseThrow(() -> new AssertionError("Class 10 was not seeded"))
        .getId();

    mockMvc.perform(put("/api/v1/students/profile")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "gender": "MALE",
              "address": {
                "street": "10 Park Street",
                "city": "Kolkata",
                "district": "Kolkata",
                "state": "West Bengal",
                "zipCode": "700016",
                "latitude": 22.5535,
                "longitude": 88.3517
              },
              "preferredMode": {
                "online": true,
                "tutorHome": false,
                "studentHome": true
              },
              "preferredRadius": 10.0,
              "classLevelId": %d,
              "boardId": %d,
              "schoolName": "Test School",
              "parentName": "Test Parent",
              "parentPhone": "9876543210",
              "parentEmail": "parent@example.com",
              "sameAsPrimaryPhone": false,
              "sameAsPrimaryEmail": false
            }
            """.formatted(classLevelId, boardId)))
        .andExpect(status().isOk());

  }

  private void configureTutorForMatching(
      String tutorToken,
      long tutorId,
      long mathematicsId) throws Exception {

    Tutor tutor = tutorRepository.findById(tutorId)
        .orElseThrow(() -> new AssertionError("Registered tutor was not found"));

    // Set up the verified state for this integration-test fixture.
    tutor.setVerificationStatus(VerificationStatus.APPROVED);
    tutorRepository.saveAndFlush(tutor);

    Long boardId = boardRepository.findByName("CBSE")
        .orElseThrow(() -> new AssertionError("CBSE board was not seeded"))
        .getId();

    Long classLevelId = classLevelRepository.findByStandard(10)
        .orElseThrow(() -> new AssertionError("Class 10 was not seeded"))
        .getId();

    mockMvc.perform(post("/api/v1/tutors/preferences")
        .header("Authorization", bearer(tutorToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "preferences": [
                {
                  "subjectId": %d,
                  "classLevelId": %d,
                  "boardId": %d
                }
              ]
            }
            """.formatted(
            mathematicsId,
            classLevelId,
            boardId)))
        .andExpect(status().isOk());
  }

  private void configureMatchingPrerequisites(
      String studentToken,
      String tutorToken,
      long tutorId,
      long mathematicsId) throws Exception {

    configureStudentForMatching(studentToken);

    configureTutorForMatching(
        tutorToken,
        tutorId,
        mathematicsId);
  }

  private void configureMatchingPrerequisites(
      String studentToken,
      String tutorToken,
      long tutorId,
      List<Long> subjectIds) throws Exception {
    configureStudentForMatching(studentToken);
    configureTutorForMatching(tutorToken, tutorId, subjectIds);
  }

  @Test
  void studentCannotCreateDuplicateActiveRequestForSameTutor() throws Exception {
    String studentToken = registerStudent();
    String tutorToken = registerTutor();
    long tutorId = tutorIdFromProfile(tutorToken);
    long mathsId = subjectId("Mathematics");

    configureMatchingPrerequisites(studentToken, tutorToken, tutorId, mathsId);
    createMatchRequest(studentToken, tutorId, mathsId);

    mockMvc.perform(post("/api/v1/matches")
        .header("Authorization", bearer(studentToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"tutorId": %d, "subjectIds": [%d],
             "message": "Duplicate active request"}
            """.formatted(tutorId, mathsId)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void tutorCanPartiallyFinalizeRequestWithMultipleSubjects() throws Exception {
    String studentToken = registerStudent();
    String tutorToken = registerTutor();
    long tutorId = tutorIdFromProfile(tutorToken);
    long mathsId = subjectId("Mathematics");
    long secondSubjectId = subjectRepository.findAll().stream()
        .map(subject -> subject.getId())
        .filter(id -> !id.equals(mathsId))
        .findFirst()
        .orElseThrow(() -> new AssertionError(
            "At least two subjects must be seeded to test partial finalization"));

    configureMatchingPrerequisites(
        studentToken, tutorToken, tutorId, List.of(mathsId, secondSubjectId));
    long requestId = createMatchRequest(
        studentToken, tutorId, List.of(mathsId, secondSubjectId));

    mockMvc.perform(put("/api/v1/matches/{requestId}/connect", requestId)
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("CONNECTED"));

    mockMvc.perform(put("/api/v1/matches/{requestId}/partial-finalize", requestId)
        .header("Authorization", bearer(tutorToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"acceptedSubjectIds": [%d]}
            """.formatted(mathsId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("PARTIALLY_FINALIZED"))
        .andExpect(jsonPath("$.data.subjects[?(@.subjectId == %d)].status"
            .formatted(mathsId)).value(org.hamcrest.Matchers.hasItem("ACCEPTED")))
        .andExpect(jsonPath("$.data.subjects[?(@.subjectId == %d)].status"
            .formatted(secondSubjectId)).value(org.hamcrest.Matchers.hasItem("REJECTED")));

    mockMvc.perform(get("/api/v1/matches/{requestId}", requestId)
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("PARTIALLY_FINALIZED"));

    mockMvc.perform(put("/api/v1/matches/{requestId}/finalize", requestId)
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("FINALIZED"))
        .andExpect(jsonPath("$.data.subjects[?(@.subjectId == %d)].status"
            .formatted(mathsId)).value(org.hamcrest.Matchers.hasItem("FINALIZED")))
        .andExpect(jsonPath("$.data.subjects[?(@.subjectId == %d)].status"
            .formatted(secondSubjectId)).value(org.hamcrest.Matchers.hasItem("REJECTED")));
  }

  @Test
  void studentCannotCancelAnotherStudentsMatchRequest() throws Exception {
    String ownerToken = registerStudent();
    String otherStudentToken = registerStudent();
    String tutorToken = registerTutor();
    long tutorId = tutorIdFromProfile(tutorToken);
    long mathsId = subjectId("Mathematics");

    configureMatchingPrerequisites(ownerToken, tutorToken, tutorId, mathsId);
    long requestId = createMatchRequest(ownerToken, tutorId, mathsId);

    mockMvc.perform(put("/api/v1/matches/{requestId}/cancel", requestId)
        .header("Authorization", bearer(otherStudentToken)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void tutorCannotFinalizeAnotherTutorsMatchRequest() throws Exception {
    String studentToken = registerStudent();
    String ownerTutorToken = registerTutor();
    String otherTutorToken = registerTutor();
    long ownerTutorId = tutorIdFromProfile(ownerTutorToken);
    long mathsId = subjectId("Mathematics");

    configureMatchingPrerequisites(studentToken, ownerTutorToken, ownerTutorId, mathsId);
    long requestId = createMatchRequest(studentToken, ownerTutorId, mathsId);

    mockMvc.perform(put("/api/v1/matches/{requestId}/connect", requestId)
        .header("Authorization", bearer(ownerTutorToken)))
        .andExpect(status().isOk());

    mockMvc.perform(put("/api/v1/matches/{requestId}/finalize", requestId)
        .header("Authorization", bearer(otherTutorToken)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void connectedRequestCannotBeCancelledByStudent() throws Exception {
    String studentToken = registerStudent();
    String tutorToken = registerTutor();
    long tutorId = tutorIdFromProfile(tutorToken);
    long mathsId = subjectId("Mathematics");

    configureMatchingPrerequisites(studentToken, tutorToken, tutorId, mathsId);
    long requestId = createMatchRequest(studentToken, tutorId, mathsId);

    mockMvc.perform(put("/api/v1/matches/{requestId}/connect", requestId)
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk());

    mockMvc.perform(put("/api/v1/matches/{requestId}/cancel", requestId)
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/api/v1/matches/{requestId}", requestId)
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("CONNECTED"));
  }

  @Test
  void tutorCannotConnectSameRequestTwice() throws Exception {
    String studentToken = registerStudent();
    String tutorToken = registerTutor();
    long tutorId = tutorIdFromProfile(tutorToken);
    long mathsId = subjectId("Mathematics");

    configureMatchingPrerequisites(studentToken, tutorToken, tutorId, mathsId);
    long requestId = createMatchRequest(studentToken, tutorId, mathsId);

    mockMvc.perform(put("/api/v1/matches/{requestId}/connect", requestId)
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk());

    mockMvc.perform(put("/api/v1/matches/{requestId}/connect", requestId)
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void studentCanCreateMatchRequestAndBothPartiesCanSeeIt()
      throws Exception {

    String studentToken = registerStudent();
    String tutorToken = registerTutor();

    long tutorId = tutorIdFromProfile(tutorToken);

    long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow(() -> new AssertionError("Mathematics was not seeded"))
        .getId();

    configureMatchingPrerequisites(
        studentToken, tutorToken, tutorId, mathematicsId);

    long requestId = createMatchRequest(
        studentToken, tutorId, mathematicsId);

    mockMvc.perform(get("/api/v1/matches/student")
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[?(@.requestId == %d)]".formatted(requestId)).isNotEmpty());

    mockMvc.perform(get("/api/v1/matches/tutor")
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[?(@.requestId == %d)]".formatted(requestId)).isNotEmpty());

    mockMvc.perform(get("/api/v1/matches/{requestId}", requestId)
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("REQUESTED"));
  }

  @Test
  void studentCannotReadAnotherStudentsMatchRequest()
      throws Exception {

    String ownerToken = registerStudent();
    String otherStudentToken = registerStudent();
    String tutorToken = registerTutor();

    long tutorId = tutorIdFromProfile(tutorToken);

    long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow(() -> new AssertionError("Mathematics was not seeded"))
        .getId();

    configureMatchingPrerequisites(
        ownerToken, tutorToken, tutorId, mathematicsId);

    long requestId = createMatchRequest(
        ownerToken, tutorId, mathematicsId);

    mockMvc.perform(get("/api/v1/matches/{requestId}", requestId)
        .header("Authorization", bearer(otherStudentToken)))
        .andExpect(result -> {
          int status = result.getResponse().getStatus();

          assertEquals(403, status,
              "Expected AccessDeniedException to map to HTTP 403. Response: "
                  + responseBody(result));
        });
  }

  @Test
  void tutorCanConnectAndFinalizeSingleSubjectMatchRequest()
      throws Exception {

    String studentToken = registerStudent();
    String tutorToken = registerTutor();

    long tutorId = tutorIdFromProfile(tutorToken);

    long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow(() -> new AssertionError("Mathematics was not seeded"))
        .getId();

    configureMatchingPrerequisites(
        studentToken, tutorToken, tutorId, mathematicsId);

    long requestId = createMatchRequest(
        studentToken, tutorId, mathematicsId);

    mockMvc.perform(put("/api/v1/matches/{requestId}/connect", requestId)
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.success").value(true));

    mockMvc.perform(get("/api/v1/matches/{requestId}", requestId)
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("CONNECTED"));

    mockMvc.perform(put(
        "/api/v1/matches/{requestId}/partial-finalize",
        requestId)
        .header("Authorization", bearer(tutorToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "acceptedSubjectIds": [%d]
            }
            """.formatted(mathematicsId)))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.success").value(true));

    /*
     * Since every requested subject is accepted, the service
     * transitions directly to FINALIZED rather than
     * PARTIALLY_FINALIZED.
     */
    mockMvc.perform(get("/api/v1/matches/{requestId}", requestId)
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("FINALIZED"));
  }

  @Test
  void tutorCannotConnectMatchRequestOwnedByAnotherTutor()
      throws Exception {

    String studentToken = registerStudent();
    String intendedTutorToken = registerTutor();
    String otherTutorToken = registerTutor();

    long intendedTutorId = tutorIdFromProfile(intendedTutorToken);

    long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow(() -> new AssertionError("Mathematics was not seeded"))
        .getId();

    configureMatchingPrerequisites(
        studentToken,
        intendedTutorToken,
        intendedTutorId,
        mathematicsId);

    long requestId = createMatchRequest(
        studentToken, intendedTutorId, mathematicsId);

    mockMvc.perform(put("/api/v1/matches/{requestId}/connect", requestId)
        .header("Authorization", bearer(otherTutorToken)))
        .andExpect(result -> {
          int status = result.getResponse().getStatus();

          assertEquals(400, status,
              "MatchRequestService throws BadRequestException for ownership violations. Response: "
                  + responseBody(result));
        });
  }

  @Test
  void studentCanCancelOwnRequestedMatchAndStatusIsPersisted()
      throws Exception {

    String studentToken = registerStudent();
    String tutorToken = registerTutor();

    long tutorId = tutorIdFromProfile(tutorToken);

    long mathematicsId = subjectRepository.findByName("Mathematics")
        .orElseThrow(() -> new AssertionError("Mathematics was not seeded"))
        .getId();

    configureMatchingPrerequisites(
        studentToken, tutorToken, tutorId, mathematicsId);

    long requestId = createMatchRequest(
        studentToken, tutorId, mathematicsId);

    mockMvc.perform(put("/api/v1/matches/{requestId}/cancel", requestId)
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("CANCELLED"));

    mockMvc.perform(get("/api/v1/matches/{requestId}", requestId)
        .header("Authorization", bearer(studentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("CANCELLED"));
  }

  @Test
  void tutorCanPurchaseBasicSubscriptionAndRetrieveCurrentAndHistory()
      throws Exception {

    String tutorToken = registerTutor();
    long tutorId = tutorIdFromProfile(tutorToken);

    Tutor tutor = tutorRepository.findById(tutorId)
        .orElseThrow(() -> new AssertionError("Registered tutor was not found"));

    tutor.setVerificationStatus(VerificationStatus.APPROVED);
    tutorRepository.saveAndFlush(tutor);

    mockMvc.perform(post("/api/v1/subscriptions")
        .header("Authorization", bearer(tutorToken))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "planType": "BASIC"
            }
            """))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.success").value(true));

    mockMvc.perform(get("/api/v1/subscriptions/current")
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isNotEmpty());

    mockMvc.perform(get("/api/v1/subscriptions/history")
        .header("Authorization", bearer(tutorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty());
  }

}
