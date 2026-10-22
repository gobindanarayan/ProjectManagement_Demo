package com.manikagro.pms.repository;

import com.manikagro.pms.domain.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUsernameIgnoreCase(String username);
    Optional<Employee> findByEmployeeCodeIgnoreCase(String employeeCode);
    Optional<Employee> findByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);
    List<Employee> findAllByOrderByNameAsc();
    List<Employee> findByActiveTrueOrderByNameAsc();
    long countByRoleAndActiveTrue(com.manikagro.pms.domain.EmployeeRole role);
}
