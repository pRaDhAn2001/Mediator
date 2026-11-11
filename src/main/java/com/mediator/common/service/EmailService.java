package com.mediator.common.service;

import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendMatchRequestNotification(
            String tutorEmail,
            String tutorName,
            String studentName,
            List<String> subjects,
            String message) {

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(tutorEmail);

        mail.setSubject(
                "New Tuition Request Received");

        String subjectText = subjects == null || subjects.isEmpty()
                ? "Requested subjects not specified"
                : String.join(", ", subjects);

        mail.setText(
                "Hello " + tutorName + ",\n\n"
                        + "You have received a new tuition request.\n\n"

                        + "Student: " + studentName + "\n"

                        + "Subjects: " + subjectText + "\n"

                        + "Message: "
                        + (message == null || message.isBlank()
                                ? "No message provided."
                                : message)

                        + "\n\n"
                        + "Please login to your tutor dashboard "
                        + "to review the request and contact the student.\n\n"

                        + "Regards,\n"
                        + "Tutor-Student Mediator");

        mailSender.send(mail);
    }
}