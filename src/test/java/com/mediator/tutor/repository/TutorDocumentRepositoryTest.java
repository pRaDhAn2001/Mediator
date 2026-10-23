package com.mediator.tutor.repository;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.tutor.entity.DocumentType;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorDocument;
import com.mediator.tutor.entity.VerificationStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TutorDocumentRepositoryTest {

    @Autowired
    private TutorDocumentRepository documentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UserRepository userRepository;

    private int counter = 0;

    private Tutor createTutor() {
        counter++;
        String suffix = String.valueOf(counter);

        User user = User.builder()
                .email("tutor" + suffix + "@example.com")
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("Tutor")
                .mobileNumber("9" + String.format("%09d", counter))
                .role(Role.ROLE_TUTOR)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        Tutor tutor = Tutor.builder()
                .user(savedUser)
                .build();

        return tutorRepository.saveAndFlush(tutor);
    }

    private TutorDocument createDocument(Tutor tutor) {
        return createDocument(tutor, DocumentType.AADHAAR_CARD);
    }

    private TutorDocument createDocument(Tutor tutor, DocumentType type) {
        TutorDocument document = TutorDocument.builder()
                .tutor(tutor)
                .documentType(type)
                .documentUrl("https://example.com/document.pdf")
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        return documentRepository.saveAndFlush(document);
    }

    @Test
    void findByTutorTutorId_shouldReturnAllDocumentsForTutor() {
        Tutor tutor = createTutor();
        createDocument(tutor, DocumentType.AADHAAR_CARD);
        createDocument(tutor, DocumentType.PAN_CARD);

        List<TutorDocument> docs = documentRepository.findByTutor_TutorId(tutor.getTutorId());

        assertThat(docs).hasSize(2);
    }

    @Test
    void findByTutorTutorId_shouldReturnEmptyWhenNoDocuments() {
        Tutor tutor = createTutor();

        List<TutorDocument> docs = documentRepository.findByTutor_TutorId(tutor.getTutorId());

        assertThat(docs).isEmpty();
    }

    @Test
    void findByDocumentIdAndTutorTutorId_shouldReturnDocument() {
        Tutor tutor = createTutor();
        TutorDocument document = createDocument(tutor);

        Optional<TutorDocument> result = documentRepository.findByDocumentIdAndTutor_TutorId(
                document.getDocumentId(),
                tutor.getTutorId());

        assertThat(result).isPresent();
        assertThat(result.get().getDocumentId())
                .isEqualTo(document.getDocumentId());
        assertThat(result.get().getTutor().getTutorId())
                .isEqualTo(tutor.getTutorId());
    }

    @Test
    void findByDocumentIdAndTutorTutorId_shouldReturnEmptyForWrongTutor() {
        Tutor owner = createTutor();
        Tutor anotherTutor = createTutor();

        TutorDocument document = createDocument(owner);

        Optional<TutorDocument> result = documentRepository.findByDocumentIdAndTutor_TutorId(
                document.getDocumentId(),
                anotherTutor.getTutorId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByDocumentIdAndTutorTutorId_shouldReturnEmptyForUnknownDocument() {
        Tutor tutor = createTutor();

        Optional<TutorDocument> result = documentRepository.findByDocumentIdAndTutor_TutorId(
                999999L,
                tutor.getTutorId());

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByDocumentIdAndTutorTutorId_shouldDeleteDocument() {
        Tutor tutor = createTutor();
        TutorDocument document = createDocument(tutor);

        documentRepository.deleteByDocumentIdAndTutor_TutorId(
                document.getDocumentId(),
                tutor.getTutorId());

        Optional<TutorDocument> result = documentRepository.findByDocumentIdAndTutor_TutorId(
                document.getDocumentId(),
                tutor.getTutorId());

        assertThat(result).isEmpty();
    }
}
