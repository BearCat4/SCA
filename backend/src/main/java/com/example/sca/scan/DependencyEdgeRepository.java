package com.example.sca.scan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DependencyEdgeRepository extends JpaRepository<DependencyEdge, Long> {
    @Query("select d from DependencyEdge d where d.scanTask.id = :scanTaskId"
            + " and (:term is null or lower(d.sourceName) like lower(concat('%', :term, '%'))"
            + " or lower(d.sourceVersion) like lower(concat('%', :term, '%'))"
            + " or lower(d.targetName) like lower(concat('%', :term, '%'))"
            + " or lower(d.targetVersion) like lower(concat('%', :term, '%'))"
            + " or lower(d.scope) like lower(concat('%', :term, '%')))")
    Page<DependencyEdge> searchByScanTaskId(@Param("scanTaskId") Long scanTaskId,
                                            @Param("term") String term,
                                            Pageable pageable);

    void deleteByScanTaskId(Long scanTaskId);
}
