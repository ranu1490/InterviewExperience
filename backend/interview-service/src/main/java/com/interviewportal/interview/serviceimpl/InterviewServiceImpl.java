package com.interviewportal.interview.serviceimpl;

import com.interviewportal.interview.ai.InterviewAnalysis;
import com.interviewportal.interview.ai.InterviewAnalyzer;
import com.interviewportal.interview.dto.InterviewRequest;
import com.interviewportal.interview.dto.InterviewResponse;
import com.interviewportal.interview.dto.InterviewSearchCriteria;
import com.interviewportal.interview.dto.InterviewSummaryResponse;
import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.entity.Interview;
import com.interviewportal.interview.entity.InterviewLike;
import com.interviewportal.interview.exception.ConflictException;
import com.interviewportal.interview.exception.ForbiddenException;
import com.interviewportal.interview.exception.NotFoundException;
import com.interviewportal.interview.mapper.InterviewMapper;
import com.interviewportal.interview.repository.CommentRepository;
import com.interviewportal.interview.repository.InterviewLikeRepository;
import com.interviewportal.interview.repository.InterviewRepository;
import com.interviewportal.interview.repository.InterviewSpecifications;
import com.interviewportal.interview.repository.ReportRepository;
import com.interviewportal.interview.security.AuthPrincipal;
import com.interviewportal.interview.service.InterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the interview lifecycle: persistence, AI enrichment, ownership rules and engagement
 * counters. All mutating methods are transactional so the entity write and its counter update
 * either both happen or neither does.
 */
@Service
public class InterviewServiceImpl implements InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);

    private final InterviewRepository interviewRepository;
    private final InterviewLikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final InterviewMapper mapper;
    private final InterviewAnalyzer analyzer;

    public InterviewServiceImpl(InterviewRepository interviewRepository,
                                InterviewLikeRepository likeRepository,
                                CommentRepository commentRepository,
                                ReportRepository reportRepository,
                                InterviewMapper mapper,
                                InterviewAnalyzer analyzer) {
        this.interviewRepository = interviewRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.reportRepository = reportRepository;
        this.mapper = mapper;
        this.analyzer = analyzer;
    }

    @Override
    @Transactional
    public InterviewResponse create(InterviewRequest request, AuthPrincipal author) {
        Interview interview = mapper.toEntity(request);
        interview.setAuthorId(author.id());
        interview.setAuthorUsername(author.username());
        if (interview.getNumberOfRounds() == null) {
            interview.setNumberOfRounds(interview.getRounds().size());
        }
        applyAnalysis(interview);
        interview = interviewRepository.save(interview);
        log.info("Created interview id={} by user={}", interview.getId(), author.id());
        return mapper.toResponse(interview, false);
    }

    @Override
    @Transactional
    public InterviewResponse update(Long id, InterviewRequest request, AuthPrincipal editor) {
        Interview interview = findOrThrow(id);
        if (!interview.getAuthorId().equals(editor.id())) {
            // Editing is owner-only by design; admins moderate by deleting, not rewriting content.
            throw new ForbiddenException("You can only edit your own posts");
        }
        mapper.updateEntity(interview, request);
        if (interview.getNumberOfRounds() == null) {
            interview.setNumberOfRounds(interview.getRounds().size());
        }
        applyAnalysis(interview);
        boolean liked = likeRepository.existsByInterviewIdAndUserId(id, editor.id());
        return mapper.toResponse(interviewRepository.save(interview), liked);
    }

    @Override
    @Transactional
    public void delete(Long id, AuthPrincipal requester, boolean asAdmin) {
        Interview interview = findOrThrow(id);
        if (!asAdmin && !interview.getAuthorId().equals(requester.id())) {
            throw new ForbiddenException("You can only delete your own posts");
        }
        // Remove dependent rows first (no DB-level FK cascade across these tables by design).
        likeRepository.deleteByInterviewId(id);
        commentRepository.deleteByInterviewId(id);
        reportRepository.deleteByInterviewId(id);
        interviewRepository.delete(interview);
        log.info("Deleted interview id={} (asAdmin={})", id, asAdmin);
    }

    @Override
    @Transactional
    public InterviewResponse getById(Long id, Long currentUserId) {
        // Atomic view increment; clears the persistence context so the reload sees the new count.
        interviewRepository.incrementViews(id);
        Interview interview = findOrThrow(id);
        boolean liked = currentUserId != null
                && likeRepository.existsByInterviewIdAndUserId(id, currentUserId);
        return mapper.toResponse(interview, liked);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InterviewSummaryResponse> search(InterviewSearchCriteria criteria,
                                                          Pageable pageable) {
        Page<Interview> page = interviewRepository.findAll(
                InterviewSpecifications.fromCriteria(criteria), pageable);
        return PagedResponse.from(page, mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InterviewSummaryResponse> findByAuthor(Long authorId, Pageable pageable) {
        Page<Interview> page = interviewRepository.findByAuthorId(authorId, pageable);
        return PagedResponse.from(page, mapper::toSummary);
    }

    @Override
    @Transactional
    public void like(Long id, AuthPrincipal user) {
        if (!interviewRepository.existsById(id)) {
            throw new NotFoundException("Interview not found: " + id);
        }
        if (likeRepository.existsByInterviewIdAndUserId(id, user.id())) {
            throw new ConflictException("You have already liked this interview");
        }
        try {
            likeRepository.save(InterviewLike.builder()
                    .interviewId(id).userId(user.id()).build());
            interviewRepository.adjustLikeCount(id, 1);
        } catch (DataIntegrityViolationException ex) {
            // The unique constraint fired on a concurrent duplicate like — treat as a conflict.
            throw new ConflictException("You have already liked this interview");
        }
    }

    @Override
    @Transactional
    public void unlike(Long id, AuthPrincipal user) {
        InterviewLike like = likeRepository.findByInterviewIdAndUserId(id, user.id())
                .orElseThrow(() -> new NotFoundException("You have not liked this interview"));
        likeRepository.delete(like);
        interviewRepository.adjustLikeCount(id, -1);
    }

    private void applyAnalysis(Interview interview) {
        InterviewAnalysis analysis = analyzer.analyze(interview);
        interview.setDifficultyScore(analysis.difficultyScore());
        interview.setDifficultyLabel(analysis.difficultyLabel());
        interview.setAiSummary(analysis.summary());
        interview.setAiSuggestedTopics(new java.util.ArrayList<>(analysis.suggestedTopics()));
    }

    private Interview findOrThrow(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Interview not found: " + id));
    }
}
