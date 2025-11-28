package com.example.skillboost.codingtest.repository;

import com.example.skillboost.codingtest.domain.CodingProblem;
import com.example.skillboost.codingtest.domain.CodingTestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingTestCaseRepository extends JpaRepository<CodingTestCase, Long> {

    List<CodingTestCase> findByProblem(CodingProblem problem);

    // 또는 problemId로 바로 찾고 싶으면
    List<CodingTestCase> findByProblem_Id(Long problemId);
}
