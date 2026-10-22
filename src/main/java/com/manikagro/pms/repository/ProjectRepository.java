package com.manikagro.pms.repository;

import com.manikagro.pms.domain.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Project> findAllByOrderByUpdatedAtDesc();
    boolean existsByManagerId(Long employeeId);
}
