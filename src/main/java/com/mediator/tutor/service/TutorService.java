package com.mediator.tutor.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.subscription.repository.SubscriptionRepository;
import com.mediator.tutor.dto.dashboard.TutorDashboardResponse;
import com.mediator.tutor.dto.document.TutorDocumentDto;
import com.mediator.tutor.entity.ProfileStatus;
import com.mediator.tutor.entity.TutorDocument;

import com.mediator.common.exception.BadRequestException;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;
import com.mediator.tutor.dto.request.TutorTeachingPreferenceRequest;
import com.mediator.tutor.entity.TutorTeachingPreference;

import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.master.repository.BoardRepository;
import com.mediator.master.repository.ClassLevelRepository;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.tutor.dto.request.TutorProfileRequest;
import com.mediator.tutor.dto.response.TutorProfileResponse;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.mapper.TutorMapper;
import com.mediator.tutor.repository.TutorDocumentRepository;
import com.mediator.tutor.repository.TutorRepository;
import com.mediator.tutor.repository.TutorTeachingPreferenceRepository;
import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TutorService {

        private final TutorRepository tutorRepository;

        private final TutorDocumentRepository tutorDocumentRepository;

        private final TutorTeachingPreferenceRepository teachingPreferenceRepository;

        private final UserRepository userRepository;

        private final TutorMapper tutorMapper;

        private final SubjectRepository subjectRepository;

        private final BoardRepository boardRepository;

        private final ClassLevelRepository classLevelRepository;

        private final SubscriptionRepository subscriptionRepository;

        /**
         * Update Tutor Profile
         */
        public TutorProfileResponse updateProfile(
                        String email,
                        TutorProfileRequest request) {

                User user = getUser(email);

                Tutor tutor = tutorRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));

                validateSalaryRange(
                                request.getSalaryMin(),
                                request.getSalaryMax());

                tutorMapper.updateTutorEntity(
                                tutor,
                                request);

                Tutor savedTutor = tutorRepository.save(tutor);

                Integer completion = calculateProfileCompletion(savedTutor);

                List<TutorDocument> documents = tutorDocumentRepository.findByTutor_TutorId(
                                savedTutor.getTutorId());

                return tutorMapper.toResponse(
                                savedTutor,
                                completion,
                                documents);
        }

        /**
         * Get Tutor Profile
         */
        @Transactional(readOnly = true)
        public TutorProfileResponse getProfile(String email) {

                User user = getUser(email);

                Tutor tutor = tutorRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));

                Integer completion = calculateProfileCompletion(tutor);

                List<TutorDocument> documents = tutorDocumentRepository.findByTutor_TutorId(
                                tutor.getTutorId());

                return tutorMapper.toResponse(
                                tutor,
                                completion,
                                documents);
        }

        /**
         * Fetch logged in user.
         */
        private User getUser(String email) {

                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found."));
        }

        /**
         * Salary Validation
         */
        private void validateSalaryRange(
                        BigDecimal salaryMin,
                        BigDecimal salaryMax) {

                if (salaryMin != null &&
                                salaryMax != null &&
                                salaryMin.compareTo(salaryMax) > 0) {

                        throw new IllegalArgumentException(
                                        "Minimum salary cannot be greater than maximum salary.");
                }
        }

        @Transactional
        public void updateTeachingPreferences(
                        String email,
                        TutorTeachingPreferenceRequest request) {

                User user = getUser(email);

                Tutor tutor = tutorRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));

                if (request.getPreferences() == null ||
                                request.getPreferences().isEmpty()) {

                        throw new BadRequestException(
                                        "At least one teaching preference is required.");
                }

                /*
                 * Delete existing preferences
                 */
                teachingPreferenceRepository.deleteByTutor_TutorId(
                                tutor.getTutorId());

                List<TutorTeachingPreference> preferences = new ArrayList<>();

                for (TutorTeachingPreferenceDto dto : request.getPreferences()) {

                        Subject subject = subjectRepository.findById(dto.getSubjectId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Subject not found."));

                        Board board = boardRepository.findById(dto.getBoardId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Board not found."));

                        ClassLevel classLevel = classLevelRepository.findById(dto.getClassLevelId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Class Level not found."));

                        TutorTeachingPreference preference = new TutorTeachingPreference();

                        preference.setTutor(tutor);

                        preference.setSubject(subject);

                        preference.setBoard(board);

                        preference.setClassLevel(classLevel);

                        preferences.add(preference);
                }

                teachingPreferenceRepository.saveAll(preferences);
        }

        @Transactional(readOnly = true)
        public List<TutorTeachingPreferenceDto> getTeachingPreferences(
                        String email) {

                User user = getUser(email);

                Tutor tutor = tutorRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));

                return teachingPreferenceRepository
                                .findByTutor_TutorId(tutor.getTutorId())
                                .stream()
                                .map(tutorMapper::toPreferenceDto)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public TutorDashboardResponse getDashboard(String email) {

                User user = getUser(email);

                Tutor tutor = tutorRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));

                Integer completion = calculateProfileCompletion(tutor);

                List<TutorDocument> documents = tutorDocumentRepository.findByTutor_TutorId(
                                tutor.getTutorId());

                TutorProfileResponse profile = tutorMapper.toResponse(
                                tutor,
                                completion,
                                documents);

                Subscription subscription = subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                tutor.getTutorId(),
                                                SubscriptionStatus.ACTIVE)
                                .orElse(null);

                return TutorDashboardResponse.builder()

                                .profile(profile)

                                // Matching module not built yet
                                .totalMatchRequests(0L)

                                .pendingMatchRequests(0L)

                                .activeStudents(0L)

                                .subscriptionStatus(
                                                subscription != null
                                                                ? subscription.getStatus()
                                                                : null)

                                .currentPlan(
                                                subscription != null
                                                                ? subscription.getSubscriptionPlan().getPlanType()
                                                                : null)

                                .verificationStatus(
                                                tutor.getVerificationStatus())

                                .build();
        }

        @Transactional
        public void uploadDocument(
                        String email,
                        TutorDocumentDto request) {

                User user = getUser(email);

                Tutor tutor = tutorRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));

                TutorDocument document = TutorDocument.builder()

                                .tutor(tutor)

                                .documentType(request.getDocumentType())

                                .documentUrl(request.getDocumentUrl())

                                .build();

                tutorDocumentRepository.save(document);
        }

        @Transactional(readOnly = true)
        public List<TutorDocumentDto> getDocuments(String email) {

                User user = getUser(email);

                Tutor tutor = tutorRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));

                return tutorDocumentRepository
                                .findByTutor_TutorId(tutor.getTutorId())
                                .stream()
                                .map(this::mapDocumentDto)
                                .toList();
        }

        @Transactional
        public void deleteDocument(
                        String email,
                        Long documentId) {

                User user = getUser(email);

                Tutor tutor = tutorRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));

                TutorDocument document = tutorDocumentRepository
                                .findByDocumentIdAndTutor_TutorId(
                                                documentId,
                                                tutor.getTutorId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Document not found."));

                tutorDocumentRepository.delete(document);
        }

        private TutorDocumentDto mapDocumentDto(
                        TutorDocument document) {

                return TutorDocumentDto.builder()

                                .documentId(document.getDocumentId())

                                .documentType(document.getDocumentType())

                                .documentUrl(document.getDocumentUrl())

                                .verificationStatus(document.getVerificationStatus())

                                .remarks(document.getRemarks())

                                .build();
        }

        private void updateProfileStatus(Tutor tutor) {

                int completion = calculateProfileCompletion(tutor);

                ProfileStatus newStatus = completion >= 80
                                ? ProfileStatus.LIVE
                                : ProfileStatus.DRAFT;

                if (tutor.getProfileStatus() != newStatus) {
                        tutor.setProfileStatus(newStatus);
                        tutorRepository.save(tutor);
                }
        }

        private Integer calculateProfileCompletion(Tutor tutor) {

                int completed = 0;
                int total = 13;

                if (tutor.getGender() != null)
                        completed++;

                if (tutor.getDescription() != null && !tutor.getDescription().isBlank())
                        completed++;

                if (tutor.getAddress() != null)
                        completed++;

                if (tutor.getAcademicProfile() != null)
                        completed++;

                if (tutor.getTeachingExperienceYears() != null)
                        completed++;

                if (tutor.getIndustryExperienceYears() != null)
                        completed++;

                if (tutor.getCurrentOccupation() != null && !tutor.getCurrentOccupation().isBlank())
                        completed++;

                if (tutor.getPreferredMode() != null)
                        completed++;

                if (tutor.getPreferredRadius() != null)
                        completed++;

                if (tutor.getSalaryMin() != null)
                        completed++;

                if (tutor.getSalaryMax() != null)
                        completed++;

                if (tutor.getDemoVideoUrl() != null && !tutor.getDemoVideoUrl().isBlank())
                        completed++;

                if (!tutorDocumentRepository.findByTutor_TutorId(tutor.getTutorId()).isEmpty())
                        completed++;

                return (completed * 100) / total;
        }

}
