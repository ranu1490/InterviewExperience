package com.interviewportal.interview.ai;

import com.interviewportal.interview.entity.DifficultyLabel;
import com.interviewportal.interview.entity.ExperienceLevel;
import com.interviewportal.interview.entity.Interview;
import com.interviewportal.interview.entity.QuestionCategory;
import com.interviewportal.interview.entity.QuestionItem;
import com.interviewportal.interview.entity.SelectionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockInterviewAnalyzerTest {

    private final MockInterviewAnalyzer analyzer = new MockInterviewAnalyzer();

    @Test
    void scoresWithinBoundsAndDerivesLabelAndTopics() {
        Interview interview = Interview.builder()
                .companyName("Google")
                .jobRole("SWE")
                .experienceLevel(ExperienceLevel.STAFF)
                .numberOfRounds(5)
                .selectionStatus(SelectionStatus.SELECTED)
                .questions(List.of(
                        new QuestionItem(QuestionCategory.DSA, "q1"),
                        new QuestionItem(QuestionCategory.HLD, "q2")))
                .build();

        InterviewAnalysis result = analyzer.analyze(interview);

        assertTrue(result.difficultyScore() >= 1 && result.difficultyScore() <= 10);
        assertEquals(DifficultyLabel.HARD, result.difficultyLabel());
        assertNotNull(result.summary());
        assertTrue(result.suggestedTopics().contains("DSA"));
        assertTrue(result.suggestedTopics().contains("HLD"));
    }

    @Test
    void simpleInterviewIsEasy() {
        Interview interview = Interview.builder()
                .companyName("Startup")
                .jobRole("Intern")
                .experienceLevel(ExperienceLevel.FRESHER)
                .numberOfRounds(0)
                .selectionStatus(SelectionStatus.REJECTED)
                .build();

        assertEquals(DifficultyLabel.EASY, analyzer.analyze(interview).difficultyLabel());
    }
}
