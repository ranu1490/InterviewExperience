package com.interviewportal.interview.controller;

import com.interviewportal.interview.dto.ReportRequest;
import com.interviewportal.interview.dto.ReportResponse;
import com.interviewportal.interview.security.CurrentUser;
import com.interviewportal.interview.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lets an authenticated user report an interview as spam/abuse. Admin review lives in AdminController. */
@RestController
@RequestMapping("/api/interviews/{interviewId}/report")
@Tag(name = "Reports", description = "Report spam/abuse for admin review")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Report an interview post")
    public ResponseEntity<ReportResponse> report(@PathVariable Long interviewId,
                                                 @Valid @RequestBody ReportRequest request) {
        ReportResponse created = reportService.report(interviewId, request, CurrentUser.require());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
