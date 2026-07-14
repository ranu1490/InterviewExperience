package com.interviewportal.interview.service;

import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.dto.ReportRequest;
import com.interviewportal.interview.dto.ReportResponse;
import com.interviewportal.interview.entity.ReportStatus;
import com.interviewportal.interview.security.AuthPrincipal;
import org.springframework.data.domain.Pageable;

/** Use-cases for spam reporting and admin moderation of reports. */
public interface ReportService {

    ReportResponse report(Long interviewId, ReportRequest request, AuthPrincipal reporter);

    PagedResponse<ReportResponse> listByStatus(ReportStatus status, Pageable pageable);

    ReportResponse updateStatus(Long reportId, ReportStatus status);
}
