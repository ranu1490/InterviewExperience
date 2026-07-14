package com.interviewportal.interview.serviceimpl;

import com.interviewportal.interview.dto.CommentRequest;
import com.interviewportal.interview.dto.CommentResponse;
import com.interviewportal.interview.dto.PagedResponse;
import com.interviewportal.interview.entity.Comment;
import com.interviewportal.interview.exception.ForbiddenException;
import com.interviewportal.interview.exception.NotFoundException;
import com.interviewportal.interview.mapper.InterviewMapper;
import com.interviewportal.interview.repository.CommentRepository;
import com.interviewportal.interview.repository.InterviewRepository;
import com.interviewportal.interview.security.AuthPrincipal;
import com.interviewportal.interview.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comment use-cases. Keeps the interview's {@code totalComments} counter in sync with each add/delete
 * so listings can show the count without a join.
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewMapper mapper;

    public CommentServiceImpl(CommentRepository commentRepository,
                              InterviewRepository interviewRepository,
                              InterviewMapper mapper) {
        this.commentRepository = commentRepository;
        this.interviewRepository = interviewRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CommentResponse add(Long interviewId, CommentRequest request, AuthPrincipal author) {
        if (!interviewRepository.existsById(interviewId)) {
            throw new NotFoundException("Interview not found: " + interviewId);
        }
        Comment comment = Comment.builder()
                .interviewId(interviewId)
                .userId(author.id())
                .username(author.username())
                .content(request.content())
                .build();
        comment = commentRepository.save(comment);
        interviewRepository.adjustCommentCount(interviewId, 1);
        return mapper.toResponse(comment);
    }

    @Override
    @Transactional
    public void delete(Long commentId, AuthPrincipal requester, boolean asAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));
        if (!asAdmin && !comment.getUserId().equals(requester.id())) {
            throw new ForbiddenException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
        interviewRepository.adjustCommentCount(comment.getInterviewId(), -1);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> list(Long interviewId, Pageable pageable) {
        Page<Comment> page = commentRepository.findByInterviewIdOrderByCreatedAtDesc(interviewId, pageable);
        return PagedResponse.from(page, mapper::toResponse);
    }
}
