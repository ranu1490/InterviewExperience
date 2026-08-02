package com.interviewportal.interview.ai;

import com.interviewportal.interview.entity.Interview;

/**
 * Produces AI-style analysis for an interview experience.
 *
 * <p>This is an interface with a swappable implementation — the seam requested in the brief.
 * Today a deterministic {@link MockInterviewAnalyzer} runs (no external calls, no cost, fully
 * testable); later an {@code OpenAiInterviewAnalyzer} can implement the same contract and be
 * activated by configuration, with zero changes to the calling service. This is the Strategy
 * pattern / Dependency Inversion in action.
 *
 * <p><b>Where AI fits in the architecture:</b> it runs synchronously inside the interview-service
 * on create/update, enriching the entity before it is saved. At higher volume this call would move
 * to an async worker (e.g. triggered off a message queue) so a slow model never blocks the write —
 * documented as the scaling path in the README.
 */
public interface InterviewAnalyzer {

    InterviewAnalysis analyze(Interview interview);
}
