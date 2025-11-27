package com.example.skillboost.codingtest.repository;

import com.example.skillboost.codingtest.domain.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Long> {
}
