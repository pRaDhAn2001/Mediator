package com.mediator.student.entity;

import com.mediator.common.base.BaseEntity;
import com.mediator.common.base.Gender;
import com.mediator.common.base.Address;
import com.mediator.common.base.PreferredMode;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.auth.entity.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
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
    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String parentPhone;

    @Email
    private String parentEmail;
}
