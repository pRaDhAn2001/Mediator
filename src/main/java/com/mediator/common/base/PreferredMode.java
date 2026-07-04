package com.mediator.common.base;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferredMode {

    // Tutor teaches online / Student prefers online
    private boolean online;

    // Tutor teaches at his/her own home
    // Student is willing to go to tutor's home
    private boolean tutorHome;

    // Tutor travels to student's home
    // Student wants tutor to come home
    private boolean studentHome;
}
