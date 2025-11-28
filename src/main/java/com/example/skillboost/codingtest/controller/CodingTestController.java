package com.example.skillboost.codingtest.controller;

import com.example.skillboost.codingtest.domain.CodingProblem;
import com.example.skillboost.codingtest.domain.Difficulty;
import com.example.skillboost.codingtest.repository.CodingProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/coding/problems")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CodingTestController {

    private final CodingProblemRepository problemRepository;

    @GetMapping("/random")
    public ResponseEntity<CodingProblem> getRandomProblem(@RequestParam(required = false) String difficulty) {
        List<CodingProblem> problems;

        // 1. 프론트에서 난이도를 선택했는지 확인
        if (difficulty != null && !difficulty.isEmpty()) {
            try {
                // "EASY" -> Difficulty.EASY 변환
                Difficulty diff = Difficulty.valueOf(difficulty.toUpperCase());
                // 해당 난이도 문제들만 DB에서 가져옴 (예: 5개)
                problems = problemRepository.findAllByDifficulty(diff);
            } catch (IllegalArgumentException e) {
                // 이상한 난이도가 오면 그냥 전체 문제 가져옴
                problems = problemRepository.findAll();
            }
        } else {
            // 난이도 선택 안 했으면 전체 문제(15개) 가져옴
            problems = problemRepository.findAll();
        }

        // 2. 문제가 하나도 없으면 404 에러
        if (problems.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 3. 목록 중에서 랜덤으로 하나 뽑기 (핵심 로직)
        Random random = new Random();
        int randomIndex = random.nextInt(problems.size()); // 0 ~ (개수-1) 사이 랜덤 숫자
        CodingProblem randomProblem = problems.get(randomIndex);

        // 4. 뽑힌 문제 반환
        return ResponseEntity.ok(randomProblem);
    }
}