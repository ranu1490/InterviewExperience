package com.interviewportal.interview.ai;

import com.interviewportal.interview.entity.DifficultyLabel;

import java.util.List;

/**
 * The result of analysing an interview: a 1-10 difficulty score, a coarse label, a short summary
 * and the key topics. Returned as an immutable value object.
 */
public record InterviewAnalysis(
        int difficultyScore,
        DifficultyLabel difficultyLabel,
        String summary,
        List<String> suggestedTopics
) {
}
