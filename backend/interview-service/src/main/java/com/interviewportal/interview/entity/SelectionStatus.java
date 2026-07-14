package com.interviewportal.interview.entity;

/**
 * Outcome of the interview.
 * {@code OFFER_REJECTED} means the candidate got an offer but declined it — distinct from being
 * rejected by the company.
 */
public enum SelectionStatus {
    SELECTED,
    REJECTED,
    OFFER_REJECTED
}
