package com.interviewportal.interview.controller;

import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.dto.ReportResponse;
import com.interviewportal.interview.entity.ReportStatus;
import com.interviewportal.interview.security.CurrentUser;
import com.interviewportal.interview.service.InterviewService;
import com.interviewportal.interview.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only moderation endpoints. The whole path is locked to ADMIN by the security config, and
 * {@code @PreAuthorize} adds a second, method-level guard (defence in depth).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Moderation: delete any post, review and resolve reports")
public class AdminController {

    private final InterviewService interviewService;
    private final ReportService reportService;

    public AdminController(InterviewService interviewService, ReportService reportService) {
        this.interviewService = interviewService;
        this.reportService = reportService;
    }

    @DeleteMapping("/interviews/{id}")
    @Operation(summary = "Delete any interview (spam removal)")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        interviewService.delete(id, CurrentUser.require(), true);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    @Operation(summary = "List reports, optionally filtered by status")
    public ResponseEntity<PagedResponse<ReportResponse>> reports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(reportService.listByStatus(status, pageable));
    }

    @PutMapping("/reports/{reportId}")
    @Operation(summary = "Update a report's status (REVIEWED / DISMISSED)")
    public ResponseEntity<ReportResponse> updateReport(@PathVariable Long reportId,
                                                       @RequestParam ReportStatus status) {
        return ResponseEntity.ok(reportService.updateStatus(reportId, status));
    }
}
