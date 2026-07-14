package com.interviewportal.interview.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Translates the product's friendly sort names into a {@link Pageable}.
 *
 * <p>Exposing "newest/oldest/mostViewed/..." rather than raw column names keeps the public API
 * decoupled from the database schema — clients don't need to know column names, and we can rename
 * columns without breaking them. Every sort targets an indexed column.
 */
final class SortResolver {

    private SortResolver() {
    }

    static Pageable resolve(int page, int size, String sort) {
        int safeSize = Math.min(Math.max(size, 1), 100); // hard cap protects the DB from huge pages
        Sort sortSpec = switch (sort == null ? "newest" : sort.toLowerCase()) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "mostviewed" -> Sort.by(Sort.Direction.DESC, "views");
            case "mosthelpful" -> Sort.by(Sort.Direction.DESC, "totalLikes");
            case "highestdifficulty" -> Sort.by(Sort.Direction.DESC, "difficultyScore");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // newest
        };
        return PageRequest.of(Math.max(page, 0), safeSize, sortSpec);
    }
}
