package com.example.sca.scan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComponentRepository extends JpaRepository<ComponentFinding, Long> {
    Page<ComponentFinding> findByScanTaskId(Long scanTaskId, Pageable pageable);
    @Query("select c from ComponentFinding c where c.scanTask.id = :scanTaskId"
            + " and (:term is null or lower(c.packageName) like lower(concat('%', :term, '%'))"
            + " or lower(c.version) like lower(concat('%', :term, '%'))"
            + " or lower(c.type) like lower(concat('%', :term, '%'))"
            + " or lower(c.target) like lower(concat('%', :term, '%')))")
    Page<ComponentFinding> searchByScanTaskId(@Param("scanTaskId") Long scanTaskId,
                                              @Param("term") String term,
                                              Pageable pageable);
    @Query("select c from ComponentFinding c where c.scanTask.project.id in :projectIds"
            + " and (:term is null or lower(c.packageName) like lower(concat('%', :term, '%'))"
            + " or lower(c.version) like lower(concat('%', :term, '%'))"
            + " or lower(c.type) like lower(concat('%', :term, '%'))"
            + " or lower(c.target) like lower(concat('%', :term, '%')))")
    Page<ComponentFinding> searchByProjectIds(@Param("projectIds") java.util.List<Long> projectIds,
                                              @Param("term") String term,
                                              Pageable pageable);
    void deleteByScanTaskId(Long scanTaskId);
}
