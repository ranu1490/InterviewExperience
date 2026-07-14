package com.interviewportal.interview.config;

import com.interviewportal.interview.ai.InterviewAnalysis;
import com.interviewportal.interview.ai.InterviewAnalyzer;
import com.interviewportal.interview.entity.ExperienceLevel;
import com.interviewportal.interview.entity.Interview;
import com.interviewportal.interview.entity.QuestionCategory;
import com.interviewportal.interview.entity.QuestionItem;
import com.interviewportal.interview.entity.RoundDetail;
import com.interviewportal.interview.entity.SelectionStatus;
import com.interviewportal.interview.repository.InterviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Seeds a couple of realistic interview experiences on first boot so the UI has content to show
 * without manual data entry. Idempotent (skips if any data exists). Dev convenience only.
 */
@Component
@Order(1)
public class SampleDataSeeder implements CommandLineRunner {

    private final InterviewRepository interviewRepository;
    private final InterviewAnalyzer analyzer;

    public SampleDataSeeder(InterviewRepository interviewRepository, InterviewAnalyzer analyzer) {
        this.interviewRepository = interviewRepository;
        this.analyzer = analyzer;
    }

    @Override
    public void run(String... args) {
        if (interviewRepository.count() > 0) {
            return;
        }

        Interview google = Interview.builder()
                .companyName("Google")
                .jobRole("Software Engineer L4")
                .experienceLevel(ExperienceLevel.MID)
                .yearsOfExperience(4.0)
                .interviewDate(LocalDate.now().minusMonths(1))
                .location("Bangalore, India")
                .ctcOffered("45 LPA")
                .numberOfRounds(5)
                .rounds(List.of(
                        new RoundDetail(1, "Phone Screen", "Two DSA problems on arrays and strings."),
                        new RoundDetail(2, "Coding 1", "Graph BFS/DFS and a dynamic programming problem."),
                        new RoundDetail(3, "Coding 2", "Trees and a hard sliding-window problem."),
                        new RoundDetail(4, "System Design", "Design a URL shortener at scale."),
                        new RoundDetail(5, "Googleyness", "Behavioural and past-project deep dive.")))
                .questions(List.of(
                        new QuestionItem(QuestionCategory.DSA, "Find the longest substring without repeating characters."),
                        new QuestionItem(QuestionCategory.DSA, "Number of islands (grid BFS)."),
                        new QuestionItem(QuestionCategory.HLD, "Design a URL shortener like TinyURL."),
                        new QuestionItem(QuestionCategory.BEHAVIORAL, "Tell me about a time you disagreed with a teammate.")))
                .overallExperience("Well structured loop. Interviewers were friendly and gave hints.")
                .preparationTips("Grind LeetCode top-150 and practise system design fundamentals.")
                .resourcesUsed(List.of("LeetCode", "Grokking the System Design Interview"))
                .selectionStatus(SelectionStatus.SELECTED)
                .tags(Set.of("faang", "dsa", "system-design"))
                .authorId(1L)
                .authorUsername("admin")
                .build();

        Interview startup = Interview.builder()
                .companyName("Razorpay")
                .jobRole("Backend Engineer")
                .experienceLevel(ExperienceLevel.JUNIOR)
                .yearsOfExperience(2.0)
                .interviewDate(LocalDate.now().minusWeeks(3))
                .location("Remote")
                .ctcOffered("22 LPA")
                .numberOfRounds(3)
                .rounds(List.of(
                        new RoundDetail(1, "DSA", "Two medium problems on hashmaps and sorting."),
                        new RoundDetail(2, "Java + Spring", "Concurrency, Spring Boot internals, JPA."),
                        new RoundDetail(3, "Hiring Manager", "Project discussion and culture fit.")))
                .questions(List.of(
                        new QuestionItem(QuestionCategory.JAVA, "Explain the internal working of a HashMap."),
                        new QuestionItem(QuestionCategory.SPRING_BOOT, "How does Spring Boot auto-configuration work?"),
                        new QuestionItem(QuestionCategory.SQL, "Write a query to find the second highest salary."),
                        new QuestionItem(QuestionCategory.HR, "Why do you want to join us?")))
                .overallExperience("Fast process, strong focus on Java internals and Spring.")
                .preparationTips("Revise core Java, Spring Boot and SQL joins thoroughly.")
                .resourcesUsed(List.of("Baeldung", "Java Brains"))
                .selectionStatus(SelectionStatus.OFFER_REJECTED)
                .tags(Set.of("java", "spring-boot", "backend"))
                .authorId(1L)
                .authorUsername("admin")
                .build();

        for (Interview interview : List.of(google, startup)) {
            InterviewAnalysis analysis = analyzer.analyze(interview);
            interview.setDifficultyScore(analysis.difficultyScore());
            interview.setDifficultyLabel(analysis.difficultyLabel());
            interview.setAiSummary(analysis.summary());
            interview.setAiSuggestedTopics(new java.util.ArrayList<>(analysis.suggestedTopics()));
            interviewRepository.save(interview);
        }
    }
}
