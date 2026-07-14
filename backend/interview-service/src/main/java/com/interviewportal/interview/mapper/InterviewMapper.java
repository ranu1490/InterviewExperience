package com.interviewportal.interview.mapper;

import com.interviewportal.interview.dto.CommentResponse;
import com.interviewportal.interview.dto.InterviewRequest;
import com.interviewportal.interview.dto.InterviewResponse;
import com.interviewportal.interview.dto.InterviewSummaryResponse;
import com.interviewportal.interview.dto.QuestionItemDto;
import com.interviewportal.interview.dto.ReportResponse;
import com.interviewportal.interview.dto.RoundDetailDto;
import com.interviewportal.interview.entity.Comment;
import com.interviewportal.interview.entity.Interview;
import com.interviewportal.interview.entity.QuestionItem;
import com.interviewportal.interview.entity.Report;
import com.interviewportal.interview.entity.RoundDetail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Converts between entities and DTOs for the interview domain.
 *
 * <p>Centralising conversion keeps controllers/services free of tedious field-copying and ensures
 * the API never accidentally leaks an entity (which could expose internal fields or trigger lazy
 * loading during serialisation).
 */
@Component
public class InterviewMapper {

    /** Builds a new entity from a create request. Author and AI fields are set by the service. */
    public Interview toEntity(InterviewRequest request) {
        return Interview.builder()
                .companyName(request.companyName())
                .companyLogo(request.companyLogo())
                .jobRole(request.jobRole())
                .experienceLevel(request.experienceLevel())
                .yearsOfExperience(request.yearsOfExperience())
                .interviewDate(request.interviewDate())
                .location(request.location())
                .ctcOffered(request.ctcOffered())
                .numberOfRounds(request.numberOfRounds())
                .rounds(toRoundEntities(request.rounds()))
                .questions(toQuestionEntities(request.questions()))
                .overallExperience(request.overallExperience())
                .preparationTips(request.preparationTips())
                .resourcesUsed(request.resourcesUsed() == null
                        ? new ArrayList<>() : new ArrayList<>(request.resourcesUsed()))
                .selectionStatus(request.selectionStatus())
                .tags(request.tags() == null ? new HashSet<>() : new HashSet<>(request.tags()))
                .build();
    }

    /** Copies writable fields from a request onto an existing entity (for updates). */
    public void updateEntity(Interview interview, InterviewRequest request) {
        interview.setCompanyName(request.companyName());
        interview.setCompanyLogo(request.companyLogo());
        interview.setJobRole(request.jobRole());
        interview.setExperienceLevel(request.experienceLevel());
        interview.setYearsOfExperience(request.yearsOfExperience());
        interview.setInterviewDate(request.interviewDate());
        interview.setLocation(request.location());
        interview.setCtcOffered(request.ctcOffered());
        interview.setNumberOfRounds(request.numberOfRounds());
        interview.setRounds(toRoundEntities(request.rounds()));
        interview.setQuestions(toQuestionEntities(request.questions()));
        interview.setOverallExperience(request.overallExperience());
        interview.setPreparationTips(request.preparationTips());
        interview.setResourcesUsed(request.resourcesUsed() == null
                ? new ArrayList<>() : new ArrayList<>(request.resourcesUsed()));
        interview.setSelectionStatus(request.selectionStatus());
        interview.setTags(request.tags() == null ? new HashSet<>() : new HashSet<>(request.tags()));
    }

    public InterviewResponse toResponse(Interview i, boolean likedByCurrentUser) {
        return new InterviewResponse(
                i.getId(), i.getCompanyName(), i.getCompanyLogo(), i.getJobRole(),
                i.getExperienceLevel(), i.getYearsOfExperience(), i.getInterviewDate(),
                i.getLocation(), i.getCtcOffered(), i.getNumberOfRounds(),
                i.getRounds().stream().map(this::toRoundDto).toList(),
                i.getQuestions().stream().map(this::toQuestionDto).toList(),
                i.getOverallExperience(), i.getPreparationTips(),
                new ArrayList<>(i.getResourcesUsed()), i.getSelectionStatus(),
                i.getDifficultyScore(), i.getDifficultyLabel(), i.getAiSummary(),
                new ArrayList<>(i.getAiSuggestedTopics()), new HashSet<>(i.getTags()),
                i.getAuthorId(), i.getAuthorUsername(), i.getTotalLikes(), i.getTotalComments(),
                i.getViews(), likedByCurrentUser, i.getCreatedAt(), i.getUpdatedAt());
    }

    public InterviewSummaryResponse toSummary(Interview i) {
        return new InterviewSummaryResponse(
                i.getId(), i.getCompanyName(), i.getCompanyLogo(), i.getJobRole(),
                i.getExperienceLevel(), i.getInterviewDate(), i.getLocation(),
                i.getSelectionStatus(), i.getDifficultyScore(), i.getDifficultyLabel(),
                new HashSet<>(i.getTags()), i.getAuthorUsername(), i.getTotalLikes(),
                i.getTotalComments(), i.getViews(), i.getCreatedAt());
    }

    public CommentResponse toResponse(Comment c) {
        return new CommentResponse(c.getId(), c.getInterviewId(), c.getUserId(),
                c.getUsername(), c.getContent(), c.getCreatedAt());
    }

    public ReportResponse toResponse(Report r) {
        return new ReportResponse(r.getId(), r.getInterviewId(), r.getReporterUserId(),
                r.getReason(), r.getStatus(), r.getCreatedAt());
    }

    private List<RoundDetail> toRoundEntities(List<RoundDetailDto> dtos) {
        List<RoundDetail> result = new ArrayList<>();
        if (dtos != null) {
            for (RoundDetailDto d : dtos) {
                result.add(new RoundDetail(d.roundNumber(), d.name(), d.description()));
            }
        }
        return result;
    }

    private List<QuestionItem> toQuestionEntities(List<QuestionItemDto> dtos) {
        List<QuestionItem> result = new ArrayList<>();
        if (dtos != null) {
            for (QuestionItemDto d : dtos) {
                result.add(new QuestionItem(d.category(), d.question()));
            }
        }
        return result;
    }

    private RoundDetailDto toRoundDto(RoundDetail r) {
        return new RoundDetailDto(r.getRoundNumber(), r.getName(), r.getDescription());
    }

    private QuestionItemDto toQuestionDto(QuestionItem q) {
        return new QuestionItemDto(q.getCategory(), q.getQuestion());
    }
}
