package com.interviewportal.interview.ai;

import com.interviewportal.interview.entity.DifficultyLabel;
import com.interviewportal.interview.entity.Interview;
import com.interviewportal.interview.entity.QuestionCategory;
import com.interviewportal.interview.entity.QuestionItem;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic, dependency-free stand-in for a real LLM analyser.
 *
 * <p>It derives difficulty from concrete signals in the post (number of rounds, breadth/volume of
 * questions, seniority and the presence of hard topics like system design), then buckets the score
 * into Easy/Medium/Hard and extracts the distinct topics that actually appear. This keeps the whole
 * app runnable and testable with no API key, while modelling exactly the output shape the real
 * integration will return.
 *
 * <p>Registered by default; the real analyser can be switched in with {@code ai.provider=openai}.
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockInterviewAnalyzer implements InterviewAnalyzer {

    private static final Set<QuestionCategory> HARD_TOPICS =
            Set.of(QuestionCategory.HLD, QuestionCategory.LLD, QuestionCategory.MICROSERVICES);

    @Override
    public InterviewAnalysis analyze(Interview interview) {
        int score = computeScore(interview);
        DifficultyLabel label = toLabel(score);
        List<String> topics = extractTopics(interview);
        String summary = buildSummary(interview, label, topics);
        return new InterviewAnalysis(score, label, summary, topics);
    }

    private int computeScore(Interview interview) {
        int score = 3; // baseline

        int rounds = interview.getNumberOfRounds() != null
                ? interview.getNumberOfRounds()
                : interview.getRounds().size();
        score += Math.min(rounds, 4);                       // more rounds -> harder, capped

        int questionCount = interview.getQuestions().size();
        score += Math.min(questionCount / 4, 2);            // more questions -> harder, capped

        boolean hasHardTopic = interview.getQuestions().stream()
                .map(QuestionItem::getCategory)
                .anyMatch(HARD_TOPICS::contains);
        if (hasHardTopic) {
            score += 1;
        }

        if (interview.getExperienceLevel() != null) {
            switch (interview.getExperienceLevel()) {
                case SENIOR -> score += 1;
                case STAFF, PRINCIPAL -> score += 2;
                default -> { /* no adjustment */ }
            }
        }
        return Math.max(1, Math.min(score, 10));
    }

    private DifficultyLabel toLabel(int score) {
        if (score <= 3) {
            return DifficultyLabel.EASY;
        }
        if (score <= 6) {
            return DifficultyLabel.MEDIUM;
        }
        return DifficultyLabel.HARD;
    }

    private List<String> extractTopics(Interview interview) {
        // Preserve insertion order but dedupe case-insensitively so, e.g., the DSA question category
        // and a "dsa" tag don't both appear as "DSA" and "Dsa".
        Set<String> seenLower = new LinkedHashSet<>();
        List<String> topics = new java.util.ArrayList<>();
        interview.getQuestions().forEach(q -> addTopic(prettify(q.getCategory()), seenLower, topics));
        if (interview.getTags() != null) {
            interview.getTags().forEach(tag -> addTopic(capitalize(tag), seenLower, topics));
        }
        return List.copyOf(topics);
    }

    private void addTopic(String topic, Set<String> seenLower, List<String> topics) {
        if (topic != null && !topic.isBlank() && seenLower.add(topic.toLowerCase())) {
            topics.add(topic);
        }
    }

    private String buildSummary(Interview interview, DifficultyLabel label, List<String> topics) {
        int rounds = interview.getNumberOfRounds() != null
                ? interview.getNumberOfRounds()
                : interview.getRounds().size();
        String topicList = topics.isEmpty() ? "general" : String.join(", ", topics);
        return String.format(
                "A %s %s interview at %s across %d round(s). Key focus areas: %s.",
                label.name().toLowerCase(),
                interview.getJobRole(),
                interview.getCompanyName(),
                rounds,
                topicList);
    }

    private String prettify(QuestionCategory category) {
        return switch (category) {
            case DSA -> "DSA";
            case SQL -> "SQL";
            case HR -> "HR";
            case LLD -> "LLD";
            case HLD -> "HLD";
            case SPRING_BOOT -> "Spring Boot";
            case MICROSERVICES -> "Microservices";
            default -> capitalize(category.name());
        };
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
    }
}
