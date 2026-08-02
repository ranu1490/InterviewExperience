package com.interviewportal.interview.serviceimpl;

import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.dto.ReportRequest;
import com.interviewportal.interview.dto.ReportResponse;
import com.interviewportal.interview.entity.Report;
import com.interviewportal.interview.entity.ReportStatus;
import com.interviewportal.interview.exception.NotFoundException;
import com.interviewportal.interview.mapper.InterviewMapper;
import com.interviewportal.interview.repository.InterviewRepository;
import com.interviewportal.interview.repository.ReportRepository;
import com.interviewportal.interview.security.AuthPrincipal;
import com.interviewportal.interview.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Spam reporting for users and moderation queue for admins. */
@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewMapper mapper;

    public ReportServiceImpl(ReportRepository reportRepository,
                             InterviewRepository interviewRepository,
                             InterviewMapper mapper) {
        this.reportRepository = reportRepository;
        this.interviewRepository = interviewRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ReportResponse report(Long interviewId, ReportRequest request, AuthPrincipal reporter) {
        if (!interviewRepository.existsById(interviewId)) {
            throw new NotFoundException("Interview not found: " + interviewId);
        }
        Report report = Report.builder()
                .interviewId(interviewId)
                .reporterUserId(reporter.id())
                .reason(request.reason())
                .status(ReportStatus.PENDING)
                .build();
        return mapper.toResponse(reportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReportResponse> listByStatus(ReportStatus status, Pageable pageable) {
        Page<Report> page = (status == null)
                ? reportRepository.findAll(pageable)
                : reportRepository.findByStatus(status, pageable);
        return PagedResponse.from(page, mapper::toResponse);
    }

    @Override
    @Transactional
    public ReportResponse updateStatus(Long reportId, ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found: " + reportId));
        report.setStatus(status);
        return mapper.toResponse(reportRepository.save(report));
    }
}
