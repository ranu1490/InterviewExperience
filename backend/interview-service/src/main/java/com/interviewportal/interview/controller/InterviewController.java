package com.interviewportal.interview.controller;

import com.interviewportal.interview.dto.InterviewRequest;
import com.interviewportal.interview.dto.InterviewResponse;
import com.interviewportal.interview.dto.InterviewSearchCriteria;
import com.interviewportal.interview.dto.InterviewSummaryResponse;
import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.entity.DifficultyLabel;
import com.interviewportal.interview.entity.ExperienceLevel;
import com.interviewportal.interview.entity.SelectionStatus;
import com.interviewportal.interview.security.CurrentUser;
import com.interviewportal.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Public + authenticated endpoints for interview experiences.
 *
 * <p>Access rules are enforced by the security config (anonymous = GET only) and, for writes, by
 * the service layer (ownership) — so this controller stays a thin HTTP adapter.
 */
@RestController
@RequestMapping("/api/interviews")
@Tag(name = "Interviews", description = "CRUD, search, likes for interview experiences")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    @Operation(summary = "List/search interviews with filters, sorting and pagination")
    public ResponseEntity<PagedResponse<InterviewSummaryResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) Double minYearsOfExperience,
            @RequestParam(required = false) Double maxYearsOfExperience,
            @RequestParam(required = false) DifficultyLabel difficultyLabel,
            @RequestParam(required = false) SelectionStatus selectionStatus,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {

        InterviewSearchCriteria criteria = new InterviewSearchCriteria(
                keyword, company, role, experienceLevel, minYearsOfExperience,
                maxYearsOfExperience, difficultyLabel, selectionStatus, location, tag, dateFrom, dateTo);
        return ResponseEntity.ok(
                interviewService.search(criteria, SortResolver.resolve(page, size, sort)));
    }

    @GetMapping("/search")
    @Operation(summary = "Alias of the list endpoint (kept for the documented /search contract)")
    public ResponseEntity<PagedResponse<InterviewSummaryResponse>> searchAlias(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) Double minYearsOfExperience,
            @RequestParam(required = false) Double maxYearsOfExperience,
            @RequestParam(required = false) DifficultyLabel difficultyLabel,
            @RequestParam(required = false) SelectionStatus selectionStatus,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return search(keyword, company, role, experienceLevel, minYearsOfExperience,
                maxYearsOfExperience, difficultyLabel, selectionStatus, location, tag,
                dateFrom, dateTo, page, size, sort);
    }

    @GetMapping("/mine")
    @Operation(summary = "List the authenticated user's own interviews")
    public ResponseEntity<PagedResponse<InterviewSummaryResponse>> myInterviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        Long userId = CurrentUser.require().id();
        return ResponseEntity.ok(
                interviewService.findByAuthor(userId, SortResolver.resolve(page, size, sort)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one interview in full detail (records a view)")
    public ResponseEntity<InterviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getById(id, CurrentUser.optionalId()));
    }

    @PostMapping
    @Operation(summary = "Create an interview experience (AI analysis runs automatically)")
    public ResponseEntity<InterviewResponse> create(@Valid @RequestBody InterviewRequest request) {
        InterviewResponse created = interviewService.create(request, CurrentUser.require());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update your own interview experience")
    public ResponseEntity<InterviewResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.ok(interviewService.update(id, request, CurrentUser.require()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete your own interview (admins may delete any)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interviewService.delete(id, CurrentUser.require(), CurrentUser.isAdmin());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like an interview")
    public ResponseEntity<Void> like(@PathVariable Long id) {
        interviewService.like(id, CurrentUser.require());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Remove your like")
    public ResponseEntity<Void> unlike(@PathVariable Long id) {
        interviewService.unlike(id, CurrentUser.require());
        return ResponseEntity.noContent().build();
    }
}
