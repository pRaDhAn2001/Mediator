package com.mediator.matching.service;

import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.common.util.DistanceUtils;
import com.mediator.matching.dto.request.TutorSearchRequest;
import com.mediator.matching.dto.response.TutorCardResponse;
import com.mediator.matching.dto.response.TutorDetailsResponse;
import com.mediator.matching.dto.response.TutorSearchResponse;
import com.mediator.matching.entity.MatchScore;
import com.mediator.matching.mapper.MatchMapper;
import com.mediator.student.entity.Student;
import com.mediator.student.repository.StudentRepository;
import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.subscription.repository.SubscriptionRepository;
import com.mediator.tutor.entity.ProfileStatus;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorTeachingPreference;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.repository.TutorRepository;
import com.mediator.tutor.repository.TutorTeachingPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

        private final TutorRepository tutorRepository;

        private final StudentRepository studentRepository;

        private final UserRepository userRepository;

        private final SubscriptionRepository subscriptionRepository;

        private final TutorTeachingPreferenceRepository tutorTeachingPreferenceRepository;

        private final MatchMapper matchMapper;

        // ============================================================
        // STUDENT TUTOR SEARCH
        // ============================================================

        @Transactional(readOnly = true)
        public TutorSearchResponse searchTutors(
                        TutorSearchRequest request) {

                validateSearchRequest(request);

                List<Tutor> tutors = tutorRepository.findAll()
                                .stream()
                                .filter(this::isTutorSearchable)
                                .filter(tutor -> matchesFilters(tutor, request))
                                .toList();

                List<MatchScore> scores = tutors.stream()
                                .map(tutor -> calculateMatchScore(tutor, request))
                                .sorted(
                                                Comparator
                                                                .comparing(
                                                                                MatchScore::getMatchedSubjects)
                                                                .reversed()
                                                                .thenComparing(
                                                                                MatchScore::getDistance,
                                                                                Comparator.nullsLast(
                                                                                                Comparator.naturalOrder()))
                                                                .thenComparing(
                                                                                MatchScore::getScore)
                                                                .reversed())
                                .toList();

                return buildSearchResponse(
                                scores,
                                request.getPage(),
                                request.getSize());
        }

        // ============================================================
        // STUDENT VIEW TUTOR DETAILS
        // ============================================================

        @Transactional(readOnly = true)
        public TutorDetailsResponse getTutorDetails(
                        String email,
                        Long tutorId) {

                Student student = getStudent(email);

                Tutor tutor = tutorRepository.findById(tutorId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tutor not found."));

                validateTutorVisibleToStudent(tutor);

                List<TutorTeachingPreference> preferences = tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(
                                                tutor.getTutorId());

                Double distance = calculateTutorDistance(
                                student,
                                tutor);

                return matchMapper.toDetails(
                                tutor,
                                distance,
                                preferences);
        }

        // ============================================================
        // SEARCH VALIDATION
        // ============================================================

        private void validateSearchRequest(
                        TutorSearchRequest request) {

                if (request.getSubjectIds() == null
                                || request.getSubjectIds().isEmpty()) {

                        throw new BadRequestException(
                                        "At least one subject is required.");
                }

                if (request.getPage() == null
                                || request.getPage() < 0) {

                        throw new BadRequestException(
                                        "Page must be zero or greater.");
                }

                if (request.getSize() == null
                                || request.getSize() <= 0) {

                        throw new BadRequestException(
                                        "Size must be greater than zero.");
                }

                if (request.getSize() > 50) {

                        throw new BadRequestException(
                                        "Maximum page size is 50.");
                }

                boolean studentHome = request.getPreferredMode() != null
                                && request.getPreferredMode()
                                                .isStudentHome();

                if (studentHome) {

                        if (request.getLatitude() == null
                                        || request.getLongitude() == null) {

                                throw new BadRequestException(
                                                "Latitude and longitude are required "
                                                                + "for student-home search.");
                        }

                        if (request.getRadius() == null
                                        || request.getRadius() <= 0) {

                                throw new BadRequestException(
                                                "Radius must be greater than zero "
                                                                + "for student-home search.");
                        }

                        if (request.getLatitude() < -90
                                        || request.getLatitude() > 90) {

                                throw new BadRequestException(
                                                "Invalid latitude.");
                        }

                        if (request.getLongitude() < -180
                                        || request.getLongitude() > 180) {

                                throw new BadRequestException(
                                                "Invalid longitude.");
                        }
                }
        }

        // ============================================================
        // SEARCHABLE TUTOR
        // ============================================================

        private boolean isTutorSearchable(
                        Tutor tutor) {

                return tutor.getVerificationStatus() == VerificationStatus.APPROVED
                                && tutor.getProfileStatus() == ProfileStatus.LIVE;
        }

        // ============================================================
        // FILTER MATCHING
        // ============================================================

        private boolean matchesFilters(
                        Tutor tutor,
                        TutorSearchRequest request) {

                if (!matchesPreferredMode(
                                tutor,
                                request)) {

                        return false;
                }

                List<TutorTeachingPreference> preferences = tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(
                                                tutor.getTutorId());

                if (preferences.isEmpty()) {
                        return false;
                }

                Set<Long> requestedSubjectIds = new HashSet<>(
                                request.getSubjectIds());

                Set<Long> matchingSubjects = new HashSet<>();

                for (TutorTeachingPreference preference : preferences) {

                        if (preference.getSubject() == null) {
                                continue;
                        }

                        if (request.getBoardId() != null
                                        && (preference.getBoard() == null
                                                        || !preference.getBoard()
                                                                        .getId()
                                                                        .equals(request.getBoardId()))) {

                                continue;
                        }

                        if (request.getClassLevelId() != null
                                        && (preference.getClassLevel() == null
                                                        || !preference.getClassLevel()
                                                                        .getId()
                                                                        .equals(
                                                                                        request.getClassLevelId()))) {

                                continue;
                        }

                        if (requestedSubjectIds.contains(
                                        preference.getSubject().getId())) {

                                matchingSubjects.add(
                                                preference.getSubject().getId());
                        }
                }

                if (matchingSubjects.isEmpty()) {
                        return false;
                }

                boolean studentHome = request.getPreferredMode() != null
                                && request.getPreferredMode()
                                                .isStudentHome();

                if (studentHome) {

                        if (tutor.getAddress() == null
                                        || tutor.getAddress()
                                                        .getLatitude() == null
                                        || tutor.getAddress()
                                                        .getLongitude() == null) {

                                return false;
                        }

                        double distance = DistanceUtils.calculateDistance(
                                        request.getLatitude(),
                                        request.getLongitude(),
                                        tutor.getAddress()
                                                        .getLatitude(),
                                        tutor.getAddress()
                                                        .getLongitude());

                        if (distance > request.getRadius()) {
                                return false;
                        }
                }

                return true;
        }

        // ============================================================
        // PREFERRED MODE
        // ============================================================

        private boolean matchesPreferredMode(
                        Tutor tutor,
                        TutorSearchRequest request) {

                if (request.getPreferredMode() == null) {
                        return true;
                }

                if (tutor.getPreferredMode() == null) {
                        return false;
                }

                boolean requestedOnline = request.getPreferredMode().isOnline();

                boolean requestedTutorHome = request.getPreferredMode().isTutorHome();

                boolean requestedStudentHome = request.getPreferredMode().isStudentHome();

                boolean matched = false;

                if (requestedOnline
                                && tutor.getPreferredMode().isOnline()) {

                        matched = true;
                }

                if (requestedTutorHome
                                && tutor.getPreferredMode().isTutorHome()) {

                        matched = true;
                }

                if (requestedStudentHome
                                && tutor.getPreferredMode().isStudentHome()) {

                        matched = true;
                }

                return matched;
        }

        // ============================================================
        // MATCH SCORE
        // ============================================================

        private MatchScore calculateMatchScore(
                        Tutor tutor,
                        TutorSearchRequest request) {

                List<TutorTeachingPreference> preferences = tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(
                                                tutor.getTutorId());

                Set<Long> requestedSubjectIds = new HashSet<>(
                                request.getSubjectIds());

                Set<Long> matchedSubjectIds = new HashSet<>();

                for (TutorTeachingPreference preference : preferences) {

                        if (preference.getSubject() == null) {
                                continue;
                        }

                        if (request.getBoardId() != null
                                        && (preference.getBoard() == null
                                                        || !preference.getBoard()
                                                                        .getId()
                                                                        .equals(request.getBoardId()))) {

                                continue;
                        }

                        if (request.getClassLevelId() != null
                                        && (preference.getClassLevel() == null
                                                        || !preference.getClassLevel()
                                                                        .getId()
                                                                        .equals(
                                                                                        request.getClassLevelId()))) {

                                continue;
                        }

                        Long subjectId = preference.getSubject().getId();

                        if (requestedSubjectIds.contains(subjectId)) {

                                matchedSubjectIds.add(subjectId);
                        }
                }

                int matchedSubjects = matchedSubjectIds.size();

                int totalRequestedSubjects = requestedSubjectIds.size();

                double matchPercentage = totalRequestedSubjects == 0
                                ? 0
                                : ((double) matchedSubjects
                                                / totalRequestedSubjects) * 100;

                Double distance = calculateSearchDistance(
                                tutor,
                                request);

                int score = calculateSecondaryScore(tutor);

                SubscriptionPlanType subscriptionPlan = getSubscriptionPlan(tutor);

                return MatchScore.builder()
                                .tutor(tutor)
                                .matchedSubjects(matchedSubjects)
                                .totalRequestedSubjects(
                                                totalRequestedSubjects)
                                .matchPercentage(
                                                Math.round(
                                                                matchPercentage * 100.0)
                                                                / 100.0)
                                .distance(distance)
                                .score(score)
                                .subscriptionPlan(
                                                subscriptionPlan)
                                .build();
        }

        // ============================================================
        // DISTANCE FOR SEARCH
        // ============================================================

        private Double calculateSearchDistance(
                        Tutor tutor,
                        TutorSearchRequest request) {

                boolean studentHome = request.getPreferredMode() != null
                                && request.getPreferredMode()
                                                .isStudentHome();

                if (!studentHome) {
                        return 0.0;
                }

                if (tutor.getAddress() == null
                                || tutor.getAddress()
                                                .getLatitude() == null
                                || tutor.getAddress()
                                                .getLongitude() == null) {

                        return Double.MAX_VALUE;
                }

                return DistanceUtils.calculateDistance(
                                request.getLatitude(),
                                request.getLongitude(),
                                tutor.getAddress()
                                                .getLatitude(),
                                tutor.getAddress()
                                                .getLongitude());
        }

        // ============================================================
        // SECONDARY SCORE
        // ============================================================

        private int calculateSecondaryScore(
                        Tutor tutor) {

                int score = 0;

                if (tutor.getVerificationStatus() == VerificationStatus.APPROVED) {

                        score += 100;
                }

                if (tutor.getTeachingExperienceYears() != null) {

                        score += Math.min(
                                        tutor.getTeachingExperienceYears()
                                                        * 5,
                                        50);
                }

                SubscriptionPlanType plan = getSubscriptionPlan(tutor);

                if (plan != null) {

                        switch (plan) {

                                case ULTRA:
                                        score += 30;
                                        break;

                                case PRO:
                                        score += 20;
                                        break;

                                case BASIC:
                                        score += 10;
                                        break;

                                default:
                                        break;
                        }
                }

                return score;
        }

        // ============================================================
        // SUBSCRIPTION
        // ============================================================

        private SubscriptionPlanType getSubscriptionPlan(
                        Tutor tutor) {

                Subscription subscription = subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                tutor.getTutorId(),
                                                SubscriptionStatus.ACTIVE)
                                .orElse(null);

                if (subscription == null
                                || subscription.getSubscriptionPlan() == null) {

                        return null;
                }

                return subscription
                                .getSubscriptionPlan()
                                .getPlanType();
        }

        // ============================================================
        // BUILD SEARCH RESPONSE
        // ============================================================

        private TutorSearchResponse buildSearchResponse(
                        List<MatchScore> scores,
                        Integer page,
                        Integer size) {

                int totalElements = scores.size();

                int totalPages = totalElements == 0
                                ? 0
                                : (int) Math.ceil(
                                                (double) totalElements
                                                                / size);

                int start = page * size;

                int end = Math.min(
                                start + size,
                                totalElements);

                List<TutorCardResponse> tutors;

                if (start >= totalElements) {

                        tutors = List.of();

                } else {

                        tutors = scores
                                        .subList(start, end)
                                        .stream()
                                        .map(score -> {

                                                List<TutorTeachingPreference> preferences = tutorTeachingPreferenceRepository
                                                                .findByTutor_TutorId(
                                                                                score.getTutor()
                                                                                                .getTutorId());

                                                return matchMapper.toCard(
                                                                score,
                                                                preferences);
                                        })
                                        .toList();
                }

                return TutorSearchResponse.builder()
                                .tutors(tutors)
                                .currentPage(page)
                                .totalPages(totalPages)
                                .totalElements(
                                                (long) totalElements)
                                .hasNext(
                                                page + 1 < totalPages)
                                .build();
        }

        // ============================================================
        // STUDENT
        // ============================================================

        private Student getStudent(
                        String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found."));

                return studentRepository
                                .findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Student profile not found."));
        }

        // ============================================================
        // TUTOR VISIBILITY
        // ============================================================

        private void validateTutorVisibleToStudent(
                        Tutor tutor) {

                if (tutor.getVerificationStatus() != VerificationStatus.APPROVED) {

                        throw new ResourceNotFoundException(
                                        "Tutor not found.");
                }

                if (tutor.getProfileStatus() != ProfileStatus.LIVE) {

                        throw new ResourceNotFoundException(
                                        "Tutor not found.");
                }
        }

        // ============================================================
        // TUTOR DISTANCE FOR DETAILS
        // ============================================================

        private Double calculateTutorDistance(
                        Student student,
                        Tutor tutor) {

                if (tutor.getPreferredMode() != null
                                && tutor.getPreferredMode().isOnline()
                                && !tutor.getPreferredMode().isStudentHome()
                                && !tutor.getPreferredMode().isTutorHome()) {

                        return 0.0;
                }

                if (student.getAddress() == null
                                || student.getAddress()
                                                .getLatitude() == null
                                || student.getAddress()
                                                .getLongitude() == null) {

                        return null;
                }

                if (tutor.getAddress() == null
                                || tutor.getAddress()
                                                .getLatitude() == null
                                || tutor.getAddress()
                                                .getLongitude() == null) {

                        return null;
                }

                return DistanceUtils.calculateDistance(
                                student.getAddress()
                                                .getLatitude(),
                                student.getAddress()
                                                .getLongitude(),
                                tutor.getAddress()
                                                .getLatitude(),
                                tutor.getAddress()
                                                .getLongitude());
        }
}
