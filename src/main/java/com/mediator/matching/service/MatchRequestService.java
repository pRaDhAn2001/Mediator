package com.mediator.matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.master.entity.Subject;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.matching.dto.request.CreateMatchRequest;
import com.mediator.matching.dto.request.TutorActionRequest;
import com.mediator.matching.dto.response.MatchRequestResponse;
import com.mediator.matching.entity.MatchRequest;
import com.mediator.matching.entity.MatchRequestStatus;
import com.mediator.matching.entity.MatchRequestSubject;
import com.mediator.matching.entity.SubjectRequestStatus;
import com.mediator.matching.mapper.MatchRequestMapper;
import com.mediator.matching.repository.MatchRequestRepository;
import com.mediator.matching.repository.MatchRequestSubjectRepository;
import com.mediator.student.entity.Student;
import com.mediator.student.repository.StudentRepository;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorTeachingPreference;
import com.mediator.tutor.repository.TutorRepository;
import com.mediator.tutor.repository.TutorTeachingPreferenceRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchRequestService {

        private final MatchRequestRepository matchRequestRepository;
        private final MatchRequestSubjectRepository matchRequestSubjectRepository;

        private final StudentRepository studentRepository;
        private final TutorRepository tutorRepository;
        private final SubjectRepository subjectRepository;
        private final UserRepository userRepository;

        private final MatchRequestMapper matchRequestMapper;

        private final TutorTeachingPreferenceRepository tutorTeachingPreferenceRepository;

        // =========================================================
        // CREATE REQUEST
        // =========================================================

        public MatchRequestResponse createRequest(
                        String email,
                        CreateMatchRequest request) {

                Student student = getStudent(email);

                Tutor tutor = tutorRepository.findById(request.getTutorId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tutor not found with ID: "
                                                                + request.getTutorId()));

                validateTutorEligibility(tutor);

                validateSubjectIds(request.getSubjectIds());

                List<MatchRequestStatus> activeStatuses = List.of(
                                MatchRequestStatus.REQUESTED,
                                MatchRequestStatus.CONNECTED,
                                MatchRequestStatus.PARTIALLY_FINALIZED);

                boolean alreadyExists = matchRequestRepository
                                .existsByStudent_StudentIdAndTutor_TutorIdAndStatusIn(
                                                student.getStudentId(),
                                                tutor.getTutorId(),
                                                activeStatuses);

                if (alreadyExists) {
                        throw new BadRequestException(
                                        "You already have an active request for this tutor.");
                }

                /*
                 * Make sure the tutor actually teaches every requested subject.
                 */
                validateTutorSubjects(
                                tutor,
                                student,
                                request.getSubjectIds());

                MatchRequest matchRequest = MatchRequest.builder()
                                .student(student)
                                .tutor(tutor)
                                .status(MatchRequestStatus.REQUESTED)
                                .message(request.getMessage())
                                .build();

                matchRequestRepository.save(matchRequest);

                List<MatchRequestSubject> requestSubjects = request.getSubjectIds()
                                .stream()
                                .map(subjectId -> {

                                        Subject subject = subjectRepository.findById(subjectId)
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Subject not found with ID: "
                                                                                        + subjectId));

                                        return MatchRequestSubject.builder()
                                                        .matchRequest(matchRequest)
                                                        .subject(subject)
                                                        .status(SubjectRequestStatus.REQUESTED)
                                                        .build();
                                })
                                .toList();

                matchRequestSubjectRepository.saveAll(requestSubjects);

                matchRequest.setRequestedSubjects(requestSubjects);

                /*
                 * IMPORTANT:
                 * Student response never contains tutor contact details.
                 */
                return matchRequestMapper.toStudentResponse(matchRequest);
        }

        // =========================================================
        // STUDENT REQUESTS
        // =========================================================

        @Transactional(readOnly = true)
        public List<MatchRequestResponse> getStudentRequests(
                        String email) {

                Student student = getStudent(email);

                return matchRequestRepository
                                .findByStudent_StudentId(student.getStudentId())
                                .stream()
                                .map(matchRequestMapper::toStudentResponse)
                                .toList();
        }

        // =========================================================
        // TUTOR REQUESTS
        // =========================================================

        @Transactional(readOnly = true)
        public List<MatchRequestResponse> getTutorRequests(
                        String email) {

                Tutor tutor = getTutor(email);

                return matchRequestRepository
                                .findByTutor_TutorId(tutor.getTutorId())
                                .stream()
                                .map(matchRequestMapper::toTutorResponse)
                                .toList();
        }

        // =========================================================
        // REQUEST DETAILS
        // =========================================================

        @Transactional(readOnly = true)
        public MatchRequestResponse getRequestDetails(
                        String email,
                        Long requestId) {

                MatchRequest request = getRequest(requestId);

                boolean isStudent = request.getStudent()
                                .getUser()
                                .getEmail()
                                .equalsIgnoreCase(email);

                boolean isTutor = request.getTutor()
                                .getUser()
                                .getEmail()
                                .equalsIgnoreCase(email);

                if (!isStudent && !isTutor) {
                        throw new BadRequestException(
                                        "You are not allowed to view this match request.");
                }

                if (isTutor) {
                        return matchRequestMapper.toTutorResponse(request);
                }

                return matchRequestMapper.toStudentResponse(request);
        }

        // =========================================================
        // CANCEL
        // =========================================================

        public MatchRequestResponse cancelRequest(
                        String email,
                        Long requestId) {

                MatchRequest request = getRequest(requestId);

                validateStudentOwnership(request, email);

                if (request.getStatus() != MatchRequestStatus.REQUESTED) {
                        throw new BadRequestException(
                                        "Only a requested match can be cancelled.");
                }

                request.setStatus(MatchRequestStatus.CANCELLED);

                request.getRequestedSubjects()
                                .forEach(subject -> subject.setStatus(SubjectRequestStatus.REJECTED));

                matchRequestRepository.save(request);

                return matchRequestMapper.toStudentResponse(request);
        }

        // =========================================================
        // CONNECT
        // =========================================================

        public MatchRequestResponse connectStudent(
                        String email,
                        Long requestId) {

                MatchRequest request = getRequest(requestId);

                validateTutorOwnership(request, email);

                if (request.getStatus() != MatchRequestStatus.REQUESTED) {
                        throw new BadRequestException(
                                        "Only a REQUESTED match can be connected.");
                }

                request.setStatus(MatchRequestStatus.CONNECTED);
                request.setConnectedAt(LocalDateTime.now());

                /*
                 * Subjects remain REQUESTED at this point.
                 *
                 * Tutor has only accepted the connection,
                 * not yet finalized individual subjects.
                 */
                matchRequestRepository.save(request);

                return matchRequestMapper.toTutorResponse(request);
        }

        // =========================================================
        // PARTIAL FINALIZE
        // =========================================================

        public MatchRequestResponse partiallyFinalize(
                        String email,
                        Long requestId,
                        TutorActionRequest action) {

                MatchRequest request = getRequest(requestId);

                validateTutorOwnership(request, email);

                if (request.getStatus() != MatchRequestStatus.CONNECTED) {
                        throw new BadRequestException(
                                        "Partial finalization is allowed only for CONNECTED requests.");
                }

                validateAcceptedSubjectIds(
                                request,
                                action.getAcceptedSubjectIds());

                Set<Long> acceptedIds = new HashSet<>(action.getAcceptedSubjectIds());

                for (MatchRequestSubject requestSubject : request.getRequestedSubjects()) {

                        Long subjectId = requestSubject.getSubject().getId();

                        if (acceptedIds.contains(subjectId)) {

                                requestSubject.setStatus(
                                                SubjectRequestStatus.ACCEPTED);

                        } else {

                                requestSubject.setStatus(
                                                SubjectRequestStatus.REJECTED);
                        }
                }

                long acceptedCount = request.getRequestedSubjects()
                                .stream()
                                .filter(subject -> subject.getStatus() == SubjectRequestStatus.ACCEPTED)
                                .count();

                if (acceptedCount == 0) {
                        throw new BadRequestException(
                                        "At least one subject must be accepted.");
                }

                if (acceptedCount == request.getRequestedSubjects().size()) {

                        /*
                         * Tutor accepted every subject.
                         *
                         * We can directly move to FINALIZED.
                         */
                        request.setStatus(
                                        MatchRequestStatus.FINALIZED);

                        request.setFinalizedAt(
                                        LocalDateTime.now());

                        request.getRequestedSubjects()
                                        .forEach(subject -> subject.setStatus(
                                                        SubjectRequestStatus.FINALIZED));

                } else {

                        /*
                         * Some subjects accepted,
                         * some rejected.
                         */
                        request.setStatus(
                                        MatchRequestStatus.PARTIALLY_FINALIZED);
                }

                matchRequestRepository.save(request);

                return matchRequestMapper.toTutorResponse(request);
        }

        // =========================================================
        // FINALIZE
        // =========================================================

        public MatchRequestResponse finalizeRequest(
                        String email,
                        Long requestId) {

                MatchRequest request = getRequest(requestId);

                validateTutorOwnership(request, email);

                if (request.getStatus() != MatchRequestStatus.CONNECTED
                                && request.getStatus() != MatchRequestStatus.PARTIALLY_FINALIZED) {

                        throw new BadRequestException(
                                        "Only CONNECTED or PARTIALLY_FINALIZED requests can be finalized.");
                }

                /*
                 * Find subjects that were accepted.
                 */
                List<MatchRequestSubject> acceptedSubjects = request.getRequestedSubjects()
                                .stream()
                                .filter(subject -> subject.getStatus() == SubjectRequestStatus.ACCEPTED)
                                .toList();

                /*
                 * If request is CONNECTED, the tutor has not selected
                 * subjects yet.
                 *
                 * In this case all requested subjects can be finalized.
                 */
                if (request.getStatus() == MatchRequestStatus.CONNECTED) {

                        request.getRequestedSubjects()
                                        .forEach(subject -> {

                                                if (subject.getStatus() == SubjectRequestStatus.REQUESTED) {

                                                        subject.setStatus(
                                                                        SubjectRequestStatus.FINALIZED);
                                                }
                                        });

                } else {

                        /*
                         * PARTIALLY_FINALIZED:
                         * only ACCEPTED subjects become FINALIZED.
                         *
                         * REJECTED subjects remain REJECTED.
                         */
                        if (acceptedSubjects.isEmpty()) {
                                throw new BadRequestException(
                                                "No accepted subjects are available for finalization.");
                        }

                        acceptedSubjects.forEach(subject -> subject.setStatus(
                                        SubjectRequestStatus.FINALIZED));
                }

                request.setStatus(
                                MatchRequestStatus.FINALIZED);

                request.setFinalizedAt(
                                LocalDateTime.now());

                matchRequestRepository.save(request);

                return matchRequestMapper.toTutorResponse(request);
        }

        // =========================================================
        // VALIDATION METHODS
        // =========================================================

        private void validateTutorEligibility(Tutor tutor) {

                /*
                 * Keep this aligned with your actual Tutor entity.
                 *
                 * Your current project uses APPROVED verification status.
                 */
                if (tutor.getVerificationStatus() == null) {
                        throw new BadRequestException(
                                        "Tutor verification status is not available.");
                }

                /*
                 * If findById() is used here, an unapproved tutor could otherwise
                 * receive requests.
                 *
                 * Replace this condition with your exact ProfileStatus logic
                 * if LIVE status is mandatory.
                 */
                if (!tutor.getVerificationStatus().name()
                                .equalsIgnoreCase("APPROVED")) {

                        throw new BadRequestException(
                                        "You cannot send a request to an unverified tutor.");
                }
        }

        private void validateSubjectIds(List<Long> subjectIds) {

                if (subjectIds == null || subjectIds.isEmpty()) {
                        throw new BadRequestException(
                                        "At least one subject is required.");
                }

                /*
                 * Prevent:
                 *
                 * [1, 1, 2]
                 */
                Set<Long> uniqueIds = new HashSet<>(subjectIds);

                if (uniqueIds.size() != subjectIds.size()) {
                        throw new BadRequestException(
                                        "Duplicate subject IDs are not allowed.");
                }
        }

        private void validateTutorSubjects(
                        Tutor tutor,
                        Student student,
                        List<Long> requestedSubjectIds) {

                // Student must have a class level configured
                if (student.getClassLevel() == null) {
                        throw new BadRequestException(
                                        "Student class level is not configured.");
                }

                // Student must have a board configured
                if (student.getBoard() == null) {
                        throw new BadRequestException(
                                        "Student board is not configured.");
                }

                // Get all teaching preferences configured for this tutor
                List<TutorTeachingPreference> tutorPreferences = tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(tutor.getTutorId());

                // If tutor has no teaching preferences at all
                if (tutorPreferences.isEmpty()) {
                        throw new BadRequestException(
                                        "Tutor has no teaching preferences configured.");
                }

                /*
                 * Keep only the tutor preferences that match
                 * the student's:
                 *
                 * 1. Class Level
                 * 2. Board
                 *
                 * Example:
                 *
                 * Student:
                 * Class 10 + CBSE
                 *
                 * Tutor:
                 * Mathematics + Class 10 + CBSE -> considered
                 * Physics + Class 10 + CBSE -> considered
                 * Chemistry + Class 12 + CBSE -> ignored
                 * Mathematics + Class 10 + ICSE -> ignored
                 */
                Set<Long> matchingTutorSubjectIds = tutorPreferences.stream()
                                .filter(preference -> preference.getClassLevel() != null
                                                && preference.getClassLevel()
                                                                .getId()
                                                                .equals(
                                                                                student.getClassLevel().getId()))
                                .filter(preference -> preference.getBoard() != null
                                                && preference.getBoard()
                                                                .getId()
                                                                .equals(
                                                                                student.getBoard().getId()))
                                .filter(preference -> preference.getSubject() != null)
                                .map(preference -> preference.getSubject().getId())
                                .collect(Collectors.toSet());

                /*
                 * Convert requested subjects to a Set.
                 *
                 * This also makes containsAll() efficient.
                 */
                Set<Long> requestedSubjectIdSet = new HashSet<>(requestedSubjectIds);

                /*
                 * Find the subjects requested by the student
                 * that the tutor does NOT teach for the
                 * student's selected class and board.
                 */
                Set<Long> unavailableSubjectIds = requestedSubjectIdSet.stream()
                                .filter(subjectId -> !matchingTutorSubjectIds
                                                .contains(subjectId))
                                .collect(Collectors.toSet());

                /*
                 * If even one requested subject is unavailable,
                 * reject the match request.
                 */
                if (!unavailableSubjectIds.isEmpty()) {
                        throw new BadRequestException(
                                        "Tutor does not teach the following requested subject IDs "
                                                        + "for the student's selected class and board: "
                                                        + unavailableSubjectIds);
                }
        }

        private void validateAcceptedSubjectIds(
                        MatchRequest request,
                        List<Long> acceptedSubjectIds) {

                if (acceptedSubjectIds == null
                                || acceptedSubjectIds.isEmpty()) {

                        throw new BadRequestException(
                                        "At least one subject must be accepted.");
                }

                Set<Long> requestedSubjectIds = request.getRequestedSubjects()
                                .stream()
                                .map(subject -> subject.getSubject().getId())
                                .collect(java.util.stream.Collectors.toSet());

                Set<Long> acceptedIds = new HashSet<>(acceptedSubjectIds);

                if (!requestedSubjectIds.containsAll(acceptedIds)) {
                        throw new BadRequestException(
                                        "Tutor can only accept subjects included in the original request.");
                }

                if (acceptedIds.size() != acceptedSubjectIds.size()) {
                        throw new BadRequestException(
                                        "Duplicate accepted subject IDs are not allowed.");
                }
        }

        // =========================================================
        // OWNERSHIP
        // =========================================================

        private void validateStudentOwnership(
                        MatchRequest request,
                        String email) {

                String studentEmail = request.getStudent()
                                .getUser()
                                .getEmail();

                if (!studentEmail.equalsIgnoreCase(email)) {
                        throw new BadRequestException(
                                        "You are not allowed to modify this request.");
                }
        }

        private void validateTutorOwnership(
                        MatchRequest request,
                        String email) {

                String tutorEmail = request.getTutor()
                                .getUser()
                                .getEmail();

                if (!tutorEmail.equalsIgnoreCase(email)) {
                        throw new BadRequestException(
                                        "You are not allowed to modify this request.");
                }
        }

        // =========================================================
        // ENTITY RETRIEVAL
        // =========================================================

        private MatchRequest getRequest(Long requestId) {

                return matchRequestRepository
                                .findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Match request not found with ID: "
                                                                + requestId));
        }

        private Student getStudent(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found."));

                return studentRepository
                                .findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Student profile not found."));
        }

        private Tutor getTutor(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found."));

                return tutorRepository
                                .findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tutor profile not found."));
        }
}