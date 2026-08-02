package com.interviewportal.interview.service;

import com.interviewportal.interview.dto.CommentRequest;
import com.interviewportal.interview.dto.CommentResponse;
import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.security.AuthPrincipal;
import org.springframework.data.domain.Pageable;

/** Use-cases for comments on interviews. */
public interface CommentService {

    CommentResponse add(Long interviewId, CommentRequest request, AuthPrincipal author);

    void delete(Long commentId, AuthPrincipal requester, boolean asAdmin);

    PagedResponse<CommentResponse> list(Long interviewId, Pageable pageable);
}
