
package com.mediator.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.mediator.common.service.EmailService;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
    }

    // ============================================================
    // SUCCESSFUL EMAIL
    // ============================================================

    @Test
    void sendMatchRequestNotification_shouldSendEmailSuccessfully() {

        emailService.sendMatchRequestNotification(
                "tutor@test.com",
                "John Tutor",
                "Jane Student",
                List.of("Mathematics", "Physics"),
                "Need tuition for Class 8");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage email = captor.getValue();

        assertThat(email.getTo())
                .containsExactly("tutor@test.com");

        assertThat(email.getSubject())
                .isEqualTo("New Tuition Request Received");

        assertThat(email.getText())
                .contains("Hello John Tutor")
                .contains("Student: Jane Student")
                .contains("Subjects: Mathematics, Physics")
                .contains("Message: Need tuition for Class 8")
                .contains("Please login to your tutor dashboard");
    }

    // ============================================================
    // SUBJECT HANDLING
    // ============================================================

    @Test
    void sendMatchRequestNotification_shouldHandleNullSubjects() {

        emailService.sendMatchRequestNotification(
                "tutor@test.com",
                "John Tutor",
                "Jane Student",
                null,
                "Need tuition");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getText())
                .contains("Subjects: Requested subjects not specified");
    }

    @Test
    void sendMatchRequestNotification_shouldHandleEmptySubjects() {

        emailService.sendMatchRequestNotification(
                "tutor@test.com",
                "John Tutor",
                "Jane Student",
                List.of(),
                "Need tuition");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getText())
                .contains("Subjects: Requested subjects not specified");
    }

    // ============================================================
    // MESSAGE HANDLING
    // ============================================================

    @Test
    void sendMatchRequestNotification_shouldHandleNullMessage() {

        emailService.sendMatchRequestNotification(
                "tutor@test.com",
                "John Tutor",
                "Jane Student",
                List.of("Mathematics"),
                null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getText())
                .contains("Message: No message provided.");
    }

    @Test
    void sendMatchRequestNotification_shouldHandleBlankMessage() {

        emailService.sendMatchRequestNotification(
                "tutor@test.com",
                "John Tutor",
                "Jane Student",
                List.of("Mathematics"),
                "   ");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getText())
                .contains("Message: No message provided.");
    }

    // ============================================================
    // EMAIL CONTENT
    // ============================================================

    @Test
    void sendMatchRequestNotification_shouldIncludeFooter() {

        emailService.sendMatchRequestNotification(
                "tutor@test.com",
                "John Tutor",
                "Jane Student",
                List.of("Mathematics"),
                "Need tuition");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getText())
                .contains("Regards,")
                .contains("Tutor-Student Mediator");
    }

    @Test
    void sendMatchRequestNotification_shouldPropagateMailSenderException() {

        doThrow(new RuntimeException("Mail server unavailable"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> emailService.sendMatchRequestNotification(
                        "tutor@test.com",
                        "John Tutor",
                        "Jane Student",
                        List.of("Mathematics"),
                        "Need tuition"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mail server unavailable");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
