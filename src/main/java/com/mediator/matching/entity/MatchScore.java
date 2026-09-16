package com.mediator.matching.entity;

import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.tutor.entity.Tutor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchScore {

    private Tutor tutor;

    /*
     * Number of requested subjects
     * that the tutor can teach for the
     * requested board/class combination.
     */
    private int matchedSubjects;

    /*
     * Total subjects requested by student.
     */
    private int totalRequestedSubjects;

    /*
     * Percentage:
     *
     * matchedSubjects / totalRequestedSubjects * 100
     */
    private double matchPercentage;

    /*
     * Distance between student and tutor.
     *
     * Unit: KM
     */
    private double distance;

    /*
     * Secondary ranking score.
     *
     * Used only AFTER:
     *
     * 1. Matched subjects
     * 2. Distance
     */
    private int score;

    /*
     * Internal use only.
     *
     * NEVER expose this through TutorCardResponse.
     */
    private SubscriptionPlanType subscriptionPlan;
}