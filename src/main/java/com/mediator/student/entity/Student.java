package com.mediator.student.entity;

import com.mediator.common.base.BaseEntity;
import com.mediator.common.base.Gender;
import com.mediator.common.base.Address;
import com.mediator.common.base.PreferredMode;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.auth.entity.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Embedded
    private Address address;

    @Embedded
    private PreferredMode preferredMode;

    private Double preferredRadius;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_level_id")
    private ClassLevel classLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    private String schoolName;

    @Column(nullable = false)
    private String parentName;

    @Column(nullable = false)
    private String parentPhone;

    private String parentEmail;
}
