package com.mediator.matching.entity;

import com.mediator.common.base.BaseEntity;
import com.mediator.student.entity.Student;
import com.mediator.tutor.entity.Tutor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MatchRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchRequestStatus status = MatchRequestStatus.REQUESTED;

    @Column(length = 500)
    private String message;

    @OneToMany(mappedBy = "matchRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchRequestSubject> requestedSubjects = new ArrayList<>();
}
