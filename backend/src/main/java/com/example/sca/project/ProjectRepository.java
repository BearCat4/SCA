package com.example.sca.project;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerIdOrderByIdDesc(Long ownerId);
    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<Project> findByTokenHash(String tokenHash);
}
