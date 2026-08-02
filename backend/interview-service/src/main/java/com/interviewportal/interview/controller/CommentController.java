package com.interviewportal.interview.controller;

import com.interviewportal.interview.dto.CommentRequest;
import com.interviewportal.interview.dto.CommentResponse;
import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.security.CurrentUser;
import com.interviewportal.interview.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Comment endpoints, nested under a specific interview. */
@RestController
@RequestMapping("/api/interviews/{interviewId}/comments")
@Tag(name = "Comments", description = "Add, list and delete comments on an interview")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "List comments (paginated, newest first)")
    public ResponseEntity<PagedResponse<CommentResponse>> list(
            @PathVariable Long interviewId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(commentService.list(interviewId,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))));
    }

    @PostMapping
    @Operation(summary = "Add a comment")
    public ResponseEntity<CommentResponse> add(@PathVariable Long interviewId,
                                               @Valid @RequestBody CommentRequest request) {
        CommentResponse created = commentService.add(interviewId, request, CurrentUser.require());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete your own comment (admins may delete any)")
    public ResponseEntity<Void> delete(@PathVariable Long interviewId, @PathVariable Long commentId) {
        commentService.delete(commentId, CurrentUser.require(), CurrentUser.isAdmin());
        return ResponseEntity.noContent().build();
    }
}
