package com.example.skillboost.codingtest.repository;

import com.example.skillboost.codingtest.domain.CodingProblem;
import com.example.skillboost.codingtest.domain.Difficulty; // ★ 이 import가 꼭 있어야 합니다
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingProblemRepository extends JpaRepository<CodingProblem, Long> {

    // 제목으로 문제 찾기 (중복 데이터 생성 방지용)
    boolean existsByTitle(String title);

    // ★ [핵심] 이 줄이 없어서 에러가 난 것입니다. 추가해주세요!
    List<CodingProblem> findAllByDifficulty(Difficulty difficulty);
}