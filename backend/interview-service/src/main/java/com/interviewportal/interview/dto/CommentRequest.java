package com.interviewportal.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload to add a comment. */
public record CommentRequest(@NotBlank @Size(max = 2000) String content) {
}
