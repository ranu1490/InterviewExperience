package com.interviewportal.interview.service;

import com.interviewportal.interview.dto.InterviewRequest;
import com.interviewportal.interview.dto.InterviewResponse;
import com.interviewportal.interview.dto.InterviewSearchCriteria;
import com.interviewportal.interview.dto.InterviewSummaryResponse;
import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.security.AuthPrincipal;
import org.springframework.data.domain.Pageable;

/** Use-cases for interview experiences and their likes. */
public interface InterviewService {

    InterviewResponse create(InterviewRequest request, AuthPrincipal author);

    InterviewResponse update(Long id, InterviewRequest request, AuthPrincipal editor);

    void delete(Long id, AuthPrincipal requester, boolean asAdmin);

    /** Returns the full detail view and records a view. {@code currentUserId} may be null (anonymous). */
    InterviewResponse getById(Long id, Long currentUserId);

    PagedResponse<InterviewSummaryResponse> search(InterviewSearchCriteria criteria, Pageable pageable);

    PagedResponse<InterviewSummaryResponse> findByAuthor(Long authorId, Pageable pageable);

    void like(Long id, AuthPrincipal user);

    void unlike(Long id, AuthPrincipal user);
}
