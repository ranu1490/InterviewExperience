package com.interviewportal.interview.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The aggregate root of the whole content domain: one shared interview experience.
 *
 * <p><b>Indexing strategy</b> (to keep search/sort fast at ~1M rows):
 * <ul>
 *   <li>{@code company_name}, {@code job_role}, {@code experience_level}, {@code selection_status},
 *       {@code location}, {@code interview_date} — the common filter columns.</li>
 *   <li>{@code created_at}, {@code views}, {@code total_likes}, {@code difficulty_score} — the sort
 *       columns (newest, most viewed, most helpful, highest difficulty).</li>
 *   <li>{@code author_id} — to list "my interviews".</li>
 * </ul>
 *
 * <p><b>Denormalised counters</b> ({@code totalLikes}, {@code totalComments}, {@code views}) are
 * stored on the row so list/detail views never run an aggregate {@code COUNT(*)} — reads dominate,
 * so we pay a tiny write cost to make reads cheap. Alternative: compute on the fly (simpler writes
 * but far slower reads); rejected because this is a read-heavy system.
 *
 * <p><b>Denormalised author</b>: we store {@code authorId} + {@code authorUsername} rather than a
 * FK to the users table, because users live in a different service/database. This is the standard
 * data-ownership trade-off in microservices.
 */
@Entity
@Table(name = "interviews",
        indexes = {
                @Index(name = "idx_interview_company", columnList = "company_name"),
                @Index(name = "idx_interview_role", columnList = "job_role"),
                @Index(name = "idx_interview_level", columnList = "experience_level"),
                @Index(name = "idx_interview_status", columnList = "selection_status"),
                @Index(name = "idx_interview_location", columnList = "location"),
                @Index(name = "idx_interview_date", columnList = "interview_date"),
                @Index(name = "idx_interview_created", columnList = "created_at"),
                @Index(name = "idx_interview_views", columnList = "views"),
                @Index(name = "idx_interview_likes", columnList = "total_likes"),
                @Index(name = "idx_interview_difficulty", columnList = "difficulty_score"),
                @Index(name = "idx_interview_author", columnList = "author_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "company_logo", length = 500)
    private String companyLogo;

    @Column(name = "job_role", nullable = false, length = 150)
    private String jobRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false, length = 20)
    private ExperienceLevel experienceLevel;

    @Column(name = "years_of_experience")
    private Double yearsOfExperience;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(length = 150)
    private String location;

    /** Free-form so any currency/format works, e.g. "32 LPA" or "$180k". */
    @Column(name = "ctc_offered", length = 60)
    private String ctcOffered;

    @Column(name = "number_of_rounds")
    private Integer numberOfRounds;

    @ElementCollection
    @CollectionTable(name = "interview_rounds", joinColumns = @JoinColumn(name = "interview_id"))
    @OrderColumn(name = "position")
    @Builder.Default
    private List<RoundDetail> rounds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "interview_questions", joinColumns = @JoinColumn(name = "interview_id"))
    @OrderColumn(name = "position")
    @Builder.Default
    private List<QuestionItem> questions = new ArrayList<>();

    @Lob
    @Column(name = "overall_experience")
    private String overallExperience;

    @Lob
    @Column(name = "preparation_tips")
    private String preparationTips;

    @ElementCollection
    @CollectionTable(name = "interview_resources", joinColumns = @JoinColumn(name = "interview_id"))
    @Column(name = "resource", length = 500)
    @Builder.Default
    private List<String> resourcesUsed = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_status", nullable = false, length = 20)
    private SelectionStatus selectionStatus;

    // ---- AI-generated fields (see InterviewAnalysisService) ----

    @Column(name = "difficulty_score")
    private Integer difficultyScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_label", length = 10)
    private DifficultyLabel difficultyLabel;

    @Lob
    @Column(name = "ai_summary")
    private String aiSummary;

    @ElementCollection
    @CollectionTable(name = "interview_ai_topics", joinColumns = @JoinColumn(name = "interview_id"))
    @Column(name = "topic", length = 60)
    @Builder.Default
    private List<String> aiSuggestedTopics = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "interview_tags", joinColumns = @JoinColumn(name = "interview_id"))
    @Column(name = "tag", length = 60)
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    // ---- Authorship & engagement ----

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "author_username", nullable = false, length = 50)
    private String authorUsername;

    @Column(name = "total_likes", nullable = false)
    @Builder.Default
    private int totalLikes = 0;

    @Column(name = "total_comments", nullable = false)
    @Builder.Default
    private int totalComments = 0;

    @Column(nullable = false)
    @Builder.Default
    private long views = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
