package com.interviewportal.interview.entity;

/**
 * The topic buckets an interview question can belong to.
 *
 * <p>Design note: rather than a separate column/table per topic (codingQuestions, dsaQuestions,
 * javaQuestions, ...), all questions live in ONE table tagged with this category. This normalises
 * ~11 near-identical fields into a single, extensible structure — adding a new topic later is an
 * enum value, not a schema migration. This directly reflects the "avoid unnecessary abstractions /
 * keep it simple" guidance while staying flexible.
 */
public enum QuestionCategory {
    CODING,
    DSA,
    JAVA,
    SPRING_BOOT,
    SQL,
    KAFKA,
    MICROSERVICES,
    LLD,
    HLD,
    HR,
    BEHAVIORAL
}
