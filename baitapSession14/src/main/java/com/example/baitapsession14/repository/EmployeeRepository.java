package com.example.baitapsession14.repository;

import com.example.baitapsession14.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> { }
