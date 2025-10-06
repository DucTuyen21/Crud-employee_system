package com.example.managelibary.repository;

import com.example.managelibary.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByEmail(String email);
    boolean existsByEmail(String email);
    List<Employee> findAllByEnabled(boolean enabled);
}