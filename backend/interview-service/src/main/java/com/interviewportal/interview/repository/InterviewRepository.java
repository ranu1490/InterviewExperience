package com.interviewportal.interview.repository;

import com.interviewportal.interview.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data access for interviews.
 *
 * <p>Extends {@link JpaSpecificationExecutor} so we can build dynamic, multi-filter search queries
 * type-safely at runtime (see {@code InterviewSpecifications}) instead of writing one giant JPQL
 * query per filter combination.
 *
 * <p>The counter updates are atomic {@code UPDATE ... SET x = x + 1} statements. Doing the
 * increment in the database (rather than read-modify-write in Java) avoids lost updates under
 * concurrency and touches only one column — cheap even at high write rates.
 */
public interface InterviewRepository
        extends JpaRepository<Interview, Long>, JpaSpecificationExecutor<Interview> {

    Page<Interview> findByAuthorId(Long authorId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Interview i set i.views = i.views + 1 where i.id = :id")
    void incrementViews(@Param("id") Long id);

    @Modifying
    @Query("update Interview i set i.totalLikes = i.totalLikes + :delta where i.id = :id")
    void adjustLikeCount(@Param("id") Long id, @Param("delta") int delta);

    @Modifying
    @Query("update Interview i set i.totalComments = i.totalComments + :delta where i.id = :id")
    void adjustCommentCount(@Param("id") Long id, @Param("delta") int delta);
}
