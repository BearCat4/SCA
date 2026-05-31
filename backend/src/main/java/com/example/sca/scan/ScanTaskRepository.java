package com.example.sca.scan;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanTaskRepository extends JpaRepository<ScanTask, Long> {
    List<ScanTask> findByProjectIdOrderByIdDesc(Long projectId);
    Optional<ScanTask> findTopByProjectIdOrderByIdDesc(Long projectId);
}
