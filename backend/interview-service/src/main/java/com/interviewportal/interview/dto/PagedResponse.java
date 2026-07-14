package com.interviewportal.interview.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * A stable, framework-neutral pagination envelope.
 *
 * <p>Why not return Spring's {@code Page} directly: its JSON shape is an internal detail that has
 * changed across versions and leaks Pageable internals. A small explicit DTO gives clients a
 * contract we control.
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <E, T> PagedResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
