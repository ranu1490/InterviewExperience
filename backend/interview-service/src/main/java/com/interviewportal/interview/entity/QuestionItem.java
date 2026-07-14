package com.interviewportal.interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single interview question tagged with its topic. Embeddable (value object) because a question
 * has no identity of its own — it only exists as part of an interview.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionItem {

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private QuestionCategory category;

    @Column(length = 2000, nullable = false)
    private String question;
}
