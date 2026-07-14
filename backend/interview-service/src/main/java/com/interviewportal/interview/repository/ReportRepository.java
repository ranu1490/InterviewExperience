package com.interviewportal.interview.repository;

import com.interviewportal.interview.entity.Report;
import com.interviewportal.interview.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for spam/abuse reports. */
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    void deleteByInterviewId(Long interviewId);
}
