package com.interviewportal.interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Describes one interview round (e.g. "Round 2 - System Design"). Value object embedded in the interview. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundDetail {

    @Column(nullable = false)
    private int roundNumber;

    @Column(length = 150)
    private String name;

    @Column(length = 4000)
    private String description;
}
