package com.interviewportal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload to report a post as spam/abuse. */
public record ReportRequest(@NotBlank @Size(max = 500) String reason) {
}
