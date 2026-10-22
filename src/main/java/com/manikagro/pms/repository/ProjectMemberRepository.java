package com.manikagro.pms.repository;

import com.manikagro.pms.domain.ProjectMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectIdOrderByAssignedDateAsc(Long projectId);
    Optional<ProjectMember> findByProjectIdAndEmployeeId(Long projectId, Long employeeId);
    long countByProjectIdAndActiveTrue(Long projectId);
    long countByProjectId(Long projectId);
    @org.springframework.data.jpa.repository.Query("select count(distinct m.employee.id) from ProjectMember m where m.active = true and m.employee.active = true")
    long countDistinctActiveEmployees();
    boolean existsByEmployeeIdAndActiveTrue(Long employeeId);
}
