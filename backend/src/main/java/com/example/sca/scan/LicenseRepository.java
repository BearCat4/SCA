package com.example.sca.scan;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LicenseRepository extends JpaRepository<LicenseFinding, Long> {
    List<LicenseFinding> findByScanTaskId(Long scanTaskId);
    Page<LicenseFinding> findByScanTaskId(Long scanTaskId, Pageable pageable);
    @Query("select l from LicenseFinding l where l.scanTask.id = :scanTaskId"
            + " and (:term is null or lower(l.packageName) like lower(concat('%', :term, '%'))"
            + " or lower(l.version) like lower(concat('%', :term, '%'))"
            + " or lower(l.target) like lower(concat('%', :term, '%')))"
            + " and (:licenseName is null or lower(l.licenseName) like lower(concat('%', :licenseName, '%')))")
    Page<LicenseFinding> searchByScanTaskId(@Param("scanTaskId") Long scanTaskId,
                                            @Param("term") String term,
                                            @Param("licenseName") String licenseName,
                                            Pageable pageable);
    @Query("select l.licenseName, count(l) from LicenseFinding l"
            + " where l.scanTask.id in :scanTaskIds"
            + " and l.licenseName is not null"
            + " and l.licenseName <> ''"
            + " group by l.licenseName"
            + " order by count(l) desc")
    List<Object[]> countByLicenseNameInLatestScans(@Param("scanTaskIds") List<Long> scanTaskIds, Pageable pageable);
    void deleteByScanTaskId(Long scanTaskId);
}
