package com.interviewportal.interview.entity;

/** Lifecycle of a spam/abuse report as it moves through admin moderation. */
public enum ReportStatus {
    PENDING,
    REVIEWED,
    DISMISSED
}
