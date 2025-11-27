package com.example.skillboost.codingtest.init;

import com.example.skillboost.codingtest.domain.CodingProblem;
import com.example.skillboost.codingtest.domain.Difficulty;
import com.example.skillboost.codingtest.repository.CodingProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodingTestDataInitializer implements CommandLineRunner {

    private final CodingProblemRepository problemRepository;

    @Override
    public void run(String... args) {
        // EASY (5문제)
        createExamSupervisorProblem();      // 시험 감독
        createZoacDistancingProblem();      // ZOAC 거리두기
        createDjmaxRankingProblem();        // DJMAX 랭킹
        createMinHeapProblem();             // 최소 힙
        createTriangleProblem();            // 삼각형 분류

        // MEDIUM (5문제)
        createSnakeGameProblem();           // Dummy (뱀 게임)
        createDiceSimulationProblem();      // 주사위 굴리기
        createTargetDistanceProblem();      // 목표지점 거리
        createDfsBfsProblem();              // DFS와 BFS
        createTripPlanningProblem();        // 여행 가자 (New)

        // HARD (5문제)
        createMarbleEscapeProblem();        // 구슬 탈출
        createSharkCopyMagicProblem();      // 마법사 상어와 복제
        createSimilarWordsProblem();        // 비슷한 단어
        createJewelThiefProblem();          // 보석 도둑
        createMarsExplorationProblem();     // 화성 탐사 (New)
    }

    // =========================
    // EASY 문제들
    // =========================

    // 1. 시험 감독
    private void createExamSupervisorProblem() {
        if (problemRepository.existsByTitle("시험 감독")) {
            return;
        }

        String description = """
                [문제]

                총 N개의 시험장이 있고, 각각의 시험장마다 응시자들이 있다. i번 시험장에 있는 응시자의 수는 Ai명이다.

                감독관은 총감독관과 부감독관으로 두 종류가 있다.
                총감독관은 한 시험장에서 감시할 수 있는 응시자의 수가 B명이고,
                부감독관은 한 시험장에서 감시할 수 있는 응시자의 수가 C명이다.

                각각의 시험장에 총감독관은 오직 1명만 있어야 하고,
                부감독관은 여러 명 있어도 된다.

                각 시험장마다 응시생들을 모두 감시해야 한다.
                이때, 필요한 감독관 수의 최솟값을 구하는 프로그램을 작성하시오.


                [입력]

                첫째 줄에 시험장의 개수 N(1 ≤ N ≤ 1,000,000)이 주어진다.
                둘째 줄에는 각 시험장에 있는 응시자의 수 Ai (1 ≤ Ai ≤ 1,000,000)가 주어진다.
                셋째 줄에는 B와 C가 주어진다. (1 ≤ B, C ≤ 1,000,000)


                [출력]

                각 시험장마다 응시생을 모두 감독하기 위해 필요한 감독관의 최소 수를 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("시험 감독")
                .difficulty(Difficulty.EASY)
                .description(description)
                .tags("math,greedy")
                .build();

        problemRepository.save(problem);
    }

    // 2. ZOAC 거리두기
    private void createZoacDistancingProblem() {
        if (problemRepository.existsByTitle("ZOAC 거리두기")) {
            return;
        }

        String description = """
                [문제]

                2021년 12월, 네 번째로 개최된 ZOAC의 오프닝을 맡은 성우는
                오프라인 대회를 대비하여 강의실을 예약하려고 한다.

                강의실에서 대회를 치르려면 거리두기 수칙을 지켜야 한다!

                한 명씩 앉을 수 있는 테이블이 행마다 W개씩 H행에 걸쳐 있을 때,
                모든 참가자는 세로로 N칸 또는 가로로 M칸 이상 비우고 앉아야 한다.
                즉, 다른 모든 참가자와 세로줄 번호의 차가 N보다 크거나
                가로줄 번호의 차가 M보다 큰 곳에만 앉을 수 있다.

                논문과 과제에 시달리는 성우를 위해
                강의실이 거리두기 수칙을 지키면서
                최대 몇 명을 수용할 수 있는지 구해보자.


                [입력]

                H, W, N, M이 공백으로 구분되어 주어진다.
                (0 < H, W, N, M ≤ 50,000)


                [출력]

                강의실이 수용할 수 있는 최대 인원 수를 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("ZOAC 거리두기")
                .difficulty(Difficulty.EASY)
                .description(description)
                .tags("math,implementation")
                .build();

        problemRepository.save(problem);
    }

    // 3. DJMAX 랭킹
    private void createDjmaxRankingProblem() {
        if (problemRepository.existsByTitle("DJMAX 랭킹")) {
            return;
        }

        String description = """
                [문제]

                태수가 즐겨하는 디제이맥스 게임은 각각의 노래마다 랭킹 리스트가 있다.
                이것은 매번 게임할 때마다 얻는 점수가 비오름차순으로 저장되어 있는 것이다.

                이 랭킹 리스트의 등수는 보통 위에서부터 몇 번째 있는 점수인지로 결정한다.
                하지만, 같은 점수가 있을 때는 그러한 점수의 등수 중에 가장 작은 등수가 된다.

                예를 들어 랭킹 리스트가 100, 90, 90, 80일 때 각각의 등수는 1, 2, 2, 4등이 된다.

                랭킹 리스트에 올라 갈 수 있는 점수의 개수 P가 주어진다.
                그리고 리스트에 있는 점수 N개가 비오름차순으로 주어지고,
                태수의 새로운 점수가 주어진다.
                이때, 태수의 새로운 점수가 랭킹 리스트에서 몇 등 하는지 구하는 프로그램을 작성하시오.
                만약 점수가 랭킹 리스트에 올라갈 수 없을 정도로 낮다면 -1을 출력한다.

                만약, 랭킹 리스트가 꽉 차있을 때,
                새 점수가 이전 점수보다 더 좋을 때만 점수가 바뀐다.


                [입력]

                첫째 줄에 N, 태수의 새로운 점수, 그리고 P가 주어진다.
                P는 10보다 크거나 같고, 50보다 작거나 같은 정수,
                N은 0보다 크거나 같고, P보다 작거나 같은 정수이다.
                그리고 모든 점수는 2,000,000,000보다 작거나 같은 자연수 또는 0이다.

                둘째 줄에는 현재 랭킹 리스트에 있는 점수가 비오름차순으로 주어진다.
                둘째 줄은 N이 0보다 큰 경우에만 주어진다.


                [출력]

                첫째 줄에 태수의 점수가 랭킹 리스트에서 차지하는 등수를 출력한다.
                랭킹 리스트에 올라갈 수 없으면 -1을 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("DJMAX 랭킹")
                .difficulty(Difficulty.EASY)
                .description(description)
                .tags("implementation,sorting")
                .build();

        problemRepository.save(problem);
    }

    // 4. 최소 힙
    private void createMinHeapProblem() {
        if (problemRepository.existsByTitle("최소 힙")) {
            return;
        }

        String description = """
                [문제]

                널리 잘 알려진 자료구조 중 최소 힙이 있다.
                최소 힙을 이용하여 다음과 같은 연산을 지원하는 프로그램을 작성하시오.

                1. 배열에 자연수 x를 넣는다.
                2. 배열에서 가장 작은 값을 출력하고, 그 값을 배열에서 제거한다.

                프로그램은 처음에 비어있는 배열에서 시작하게 된다.


                [입력]

                첫째 줄에 연산의 개수 N(1 ≤ N ≤ 100,000)이 주어진다.
                다음 N개의 줄에는 연산에 대한 정보를 나타내는 정수 x가 주어진다.
                만약 x가 자연수라면 배열에 x라는 값을 넣는(추가하는) 연산이고,
                x가 0이라면 배열에서 가장 작은 값을 출력하고 그 값을 배열에서 제거하는 경우이다.
                x는 2^31보다 작은 자연수 또는 0이고, 음의 정수는 입력으로 주어지지 않는다.


                [출력]

                입력에서 0이 주어진 횟수만큼 답을 출력한다.
                만약 배열이 비어 있는 경우인데 가장 작은 값을 출력하라고 한 경우에는 0을 출력하면 된다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("최소 힙")
                .difficulty(Difficulty.EASY)
                .description(description)
                .tags("datastructure,heap")
                .build();

        problemRepository.save(problem);
    }

    // 5. 삼각형 분류
    private void createTriangleProblem() {
        if (problemRepository.existsByTitle("삼각형 분류")) {
            return;
        }

        String description = """
                [문제]

                삼각형의 세 변의 길이가 주어질 때 변의 길이에 따라 다음과 같이 정의한다.
                Equilateral : 세 변의 길이가 모두 같은 경우
                Isosceles : 두 변의 길이만 같은 경우
                Scalene : 세 변의 길이가 모두 다른 경우
                
                단 주어진 세 변의 길이가 삼각형의 조건을 만족하지 못하는 경우에는 "Invalid" 를 출력한다.
                예를 들어 6, 3, 2가 이 경우에 해당한다.
                가장 긴 변의 길이보다 나머지 두 변의 길이의 합이 길지 않으면 삼각형의 조건을 만족하지 못한다.

                세 변의 길이가 주어질 때 위 정의에 따른 결과를 출력하시오.


                [입력]

                각 줄에는 1,000을 넘지 않는 양의 정수 3개가 입력된다.
                마지막 줄은 0 0 0이며 이 줄은 계산하지 않는다.


                [출력]

                각 입력에 대해 Equilateral, Isosceles, Scalene, Invalid 중 하나를 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("삼각형 분류")
                .difficulty(Difficulty.EASY)
                .description(description)
                .tags("math,implementation,geometry")
                .build();

        problemRepository.save(problem);
    }

    // =========================
    // MEDIUM 문제들
    // =========================

    // 6. Dummy (뱀 게임)
    private void createSnakeGameProblem() {
        if (problemRepository.existsByTitle("Dummy (뱀 게임)")) {
            return;
        }

        String description = """
                [문제]

                'Dummy' 라는 도스게임이 있다. 이 게임에는 뱀이 나와서 기어다니는데,
                사과를 먹으면 뱀 길이가 늘어난다.
                뱀이 이리저리 기어다니다가 벽 또는 자기자신의 몸과 부딪히면 게임이 끝난다.

                게임은 NxN 정사각 보드 위에서 진행되고, 몇몇 칸에는 사과가 놓여져 있다.
                보드의 상하좌우 끝에는 벽이 있다.
                게임이 시작할 때 뱀은 맨 위 맨 좌측에 위치하고 뱀의 길이는 1이다.
                뱀은 처음에 오른쪽을 향한다.

                뱀은 매 초마다 이동을 하는데 다음과 같은 규칙을 따른다.

                1. 먼저 뱀은 몸길이를 늘려 머리를 다음 칸에 위치시킨다.
                2. 만약 벽이나 자기자신의 몸과 부딪히면 게임이 끝난다.
                3. 만약 이동한 칸에 사과가 있다면, 그 칸에 있던 사과가 없어지고 꼬리는 움직이지 않는다.
                4. 만약 이동한 칸에 사과가 없다면, 몸길이를 줄여서 꼬리가 위치한 칸을 비워준다. 즉, 몸길이는 변하지 않는다.

                사과의 위치와 뱀의 이동경로가 주어질 때
                이 게임이 몇 초에 끝나는지 계산하라.


                [입력]

                첫째 줄에 보드의 크기 N이 주어진다. (2 ≤ N ≤ 100)
                다음 줄에 사과의 개수 K가 주어진다. (0 ≤ K ≤ 100)

                다음 K개의 줄에는 사과의 위치가 주어진다.
                첫 번째 정수는 행, 두 번째 정수는 열 위치를 의미한다.
                사과의 위치는 모두 다르며, 맨 위 맨 좌측 (1행 1열)에는 사과가 없다.

                다음 줄에는 뱀의 방향 변환 횟수 L이 주어진다. (1 ≤ L ≤ 100)

                다음 L개의 줄에는 뱀의 방향 변환 정보가 주어진다.
                정수 X와 문자 C로 이루어져 있으며,
                게임 시작 시간으로부터 X초가 끝난 뒤에
                왼쪽(C가 'L') 또는 오른쪽(C가 'D')으로 90도 방향을 회전시킨다는 뜻이다.
                X는 10,000 이하의 양의 정수이며, 방향 전환 정보는 X가 증가하는 순으로 주어진다.


                [출력]

                첫째 줄에 게임이 몇 초에 끝나는지 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("Dummy (뱀 게임)")
                .difficulty(Difficulty.MEDIUM)
                .description(description)
                .tags("simulation,implementation,queue")
                .build();

        problemRepository.save(problem);
    }

    // 7. 주사위 굴리기
    private void createDiceSimulationProblem() {
        if (problemRepository.existsByTitle("주사위 굴리기")) {
            return;
        }

        String description = """
                [문제]

                크기가 N×M인 지도가 존재한다. 지도의 오른쪽은 동쪽, 위쪽은 북쪽이다.
                이 지도의 위에 주사위가 하나 놓여져 있으며, 주사위의 전개도는 아래와 같다.
                지도의 좌표는 (r, c)로 나타내며, r는 북쪽으로부터 떨어진 칸의 개수,
                c는 서쪽으로부터 떨어진 칸의 개수이다.

                      2
                4  1  3
                      5
                      6

                주사위는 지도 위에 윗 면이 1이고, 동쪽을 바라보는 방향이 3인 상태로 놓여져 있으며,
                놓여져 있는 곳의 좌표는 (x, y)이다.
                가장 처음에 주사위에는 모든 면에 0이 적혀져 있다.

                지도의 각 칸에는 정수가 하나씩 쓰여져 있다.
                주사위를 굴렸을 때, 이동한 칸에 쓰여 있는 수가 0이면,
                주사위의 바닥면에 쓰여 있는 수가 칸에 복사된다.
                0이 아닌 경우에는 칸에 쓰여 있는 수가 주사위의 바닥면으로 복사되며,
                칸에 쓰여 있는 수는 0이 된다.

                주사위를 놓은 곳의 좌표와 이동시키는 명령이 주어졌을 때,
                주사위가 이동했을 때마다 상단에 쓰여 있는 값을 구하는 프로그램을 작성하시오.

                주사위는 지도의 바깥으로 이동시킬 수 없다.
                만약 바깥으로 이동시키려고 하는 경우에는 해당 명령을 무시해야 하며,
                출력도 하면 안 된다.


                [입력]

                첫째 줄에 지도의 세로 크기 N, 가로 크기 M (1 ≤ N, M ≤ 20),
                주사위를 놓은 곳의 좌표 x, y(0 ≤ x ≤ N-1, 0 ≤ y ≤ M-1),
                그리고 명령의 개수 K (1 ≤ K ≤ 1,000)가 주어진다.

                둘째 줄부터 N개의 줄에 지도에 쓰여 있는 수가 북쪽부터 남쪽으로,
                각 줄은 서쪽부터 동쪽 순서대로 주어진다.
                주사위를 놓은 칸에 쓰여 있는 수는 항상 0이다.
                지도의 각 칸에 쓰여 있는 수는 10 미만의 자연수 또는 0이다.

                마지막 줄에는 이동하는 명령이 순서대로 주어진다.
                동쪽은 1, 서쪽은 2, 북쪽은 3, 남쪽은 4로 주어진다.


                [출력]

                이동할 때마다 주사위의 윗 면에 쓰여 있는 수를 출력한다.
                만약 바깥으로 이동시키려고 하는 경우에는 해당 명령을 무시해야 하며,
                출력도 하면 안 된다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("주사위 굴리기")
                .difficulty(Difficulty.MEDIUM)
                .description(description)
                .tags("simulation,implementation")
                .build();

        problemRepository.save(problem);
    }

    // 8. 목표지점 거리
    private void createTargetDistanceProblem() {
        if (problemRepository.existsByTitle("목표지점 거리")) {
            return;
        }

        String description = """
                [문제]

                지도가 주어지면 모든 지점에 대해서 목표지점까지의 거리를 구하여라.
                문제를 쉽게 만들기 위해 오직 가로와 세로로만 움직일 수 있다고 하자.

                [입력]

                지도의 크기 n과 m이 주어진다. n은 세로의 크기, m은 가로의 크기다.(2 ≤ n ≤ 1000, 2 ≤ m ≤ 1000)
                다음 n개의 줄에 m개의 숫자가 주어진다. 0은 갈 수 없는 땅이고 1은 갈 수 있는 땅, 2는 목표지점이다. 입력에서 2는 단 한개이다.

                [출력]

                각 지점에서 목표지점까지의 거리를 출력한다.
                원래 갈 수 없는 땅인 위치는 0을 출력하고, 원래 갈 수 있는 땅인 부분 중에서 도달할 수 없는 위치는 -1을 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("목표지점 거리")
                .difficulty(Difficulty.MEDIUM)
                .description(description)
                .tags("bfs,graph")
                .build();

        problemRepository.save(problem);
    }

    // 9. DFS와 BFS
    private void createDfsBfsProblem() {
        if (problemRepository.existsByTitle("DFS와 BFS")) {
            return;
        }

        String description = """
                [문제]

                그래프를 DFS로 탐색한 결과와 BFS로 탐색한 결과를 출력하는 프로그램을 작성하시오.
                단, 방문할 수 있는 정점이 여러 개인 경우에는 정점 번호가 작은 것을 먼저 방문하고,
                더 이상 방문할 수 있는 점이 없는 경우 종료한다.
                정점 번호는 1번부터 N번까지이다.


                [입력]

                첫째 줄에 정점의 개수 N(1 ≤ N ≤ 1,000), 간선의 개수 M(1 ≤ M ≤ 10,000), 탐색을 시작할 정점의 번호 V가 주어진다.
                다음 M개의 줄에는 간선이 연결하는 두 정점의 번호가 주어진다.
                어떤 두 정점 사이에 여러 개의 간선이 있을 수 있다. 입력으로 주어지는 간선은 양방향이다.


                [출력]

                첫째 줄에 DFS를 수행한 결과를, 그 다음 줄에는 BFS를 수행한 결과를 출력한다.
                V부터 방문된 점을 순서대로 출력하면 된다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("DFS와 BFS")
                .difficulty(Difficulty.MEDIUM)
                .description(description)
                .tags("graph,dfs,bfs")
                .build();

        problemRepository.save(problem);
    }

    // 10. 여행 가자 (New)
    private void createTripPlanningProblem() {
        if (problemRepository.existsByTitle("여행 가자")) {
            return;
        }

        String description = """
                [문제]

                동혁이는 친구들과 함께 여행을 가려고 한다. 한국에는 도시가 N개 있고 임의의 두 도시 사이에 길이 있을 수도, 없을 수도 있다.
                동혁이의 여행 일정이 주어졌을 때, 이 여행 경로가 가능한 것인지 알아보자. 물론 중간에 다른 도시를 경유해서 여행을 할 수도 있다.
                예를 들어 도시가 5개 있고, A-B, B-C, A-D, B-D, E-A의 길이 있고, 동혁이의 여행 계획이 E C B C D 라면 E-A-B-C-B-C-B-D라는 여행경로를 통해 목적을 달성할 수 있다.

                도시들의 개수와 도시들 간의 연결 여부가 주어져 있고, 동혁이의 여행 계획에 속한 도시들이 순서대로 주어졌을 때 가능한지 여부를 판별하는 프로그램을 작성하시오.
                같은 도시를 여러 번 방문하는 것도 가능하다.


                [입력]

                첫 줄에 도시의 수 N이 주어진다. N은 200이하이다. 둘째 줄에 여행 계획에 속한 도시들의 수 M이 주어진다. M은 1000이하이다.
                다음 N개의 줄에는 N개의 정수가 주어진다. i번째 줄의 j번째 수는 i번 도시와 j번 도시의 연결 정보를 의미한다.
                1이면 연결된 것이고 0이면 연결이 되지 않은 것이다. A와 B가 연결되었으면 B와 A도 연결되어 있다.
                마지막 줄에는 여행 계획이 주어진다. 도시의 번호는 1부터 N까지 차례대로 매겨져 있다.


                [출력]

                첫 줄에 가능하면 YES 불가능하면 NO를 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("여행 가자")
                .difficulty(Difficulty.MEDIUM)
                .description(description)
                .tags("graph,union_find,bfs")
                .build();

        problemRepository.save(problem);
    }

    // =========================
    // HARD 문제들
    // =========================

    // 11. 구슬 탈출
    private void createMarbleEscapeProblem() {
        if (problemRepository.existsByTitle("구슬 탈출")) {
            return;
        }

        String description = """
                [문제]

                스타트링크에서 판매하는 어린이용 장난감 중에서 가장 인기가 많은 제품은 구슬 탈출이다.
                구슬 탈출은 직사각형 보드에 빨간 구슬과 파란 구슬을 하나씩 넣은 다음,
                빨간 구슬을 구멍을 통해 빼내는 게임이다.

                보드의 세로 크기는 N, 가로 크기는 M이고, 편의상 1×1 크기의 칸으로 나누어져 있다.
                가장 바깥 행과 열은 모두 막혀져 있고, 보드에는 구멍이 하나 있다.
                빨간 구슬과 파란 구슬의 크기는 보드에서 1×1 크기의 칸을 가득 채우는 사이즈이고,
                각각 하나씩 들어가 있다.

                이때, 구슬을 손으로 건드릴 수는 없고, 중력을 이용해서 이리 저리 굴려야 한다.
                왼쪽으로 기울이기, 오른쪽으로 기울이기, 위쪽으로 기울이기,
                아래쪽으로 기울이기와 같은 네 가지 동작이 가능하다.

                각각의 동작에서 공은 동시에 움직인다.
                빨간 구슬이 구멍에 빠지면 성공이지만, 파란 구슬이 구멍에 빠지면 실패이다.
                빨간 구슬과 파란 구슬이 동시에 구멍에 빠져도 실패이다.
                빨간 구슬과 파란 구슬은 동시에 같은 칸에 있을 수 없다.
                또, 빨간 구슬과 파란 구슬의 크기는 한 칸을 모두 차지한다.
                기울이는 동작을 그만하는 것은 더 이상 구슬이 움직이지 않을 때까지이다.

                보드의 상태가 주어졌을 때,
                최소 몇 번 만에 빨간 구슬을 구멍을 통해 빼낼 수 있는지 구하는 프로그램을 작성하시오.


                [입력]

                첫 번째 줄에는 보드의 세로, 가로 크기를 의미하는 두 정수 N, M (3 ≤ N, M ≤ 10)이 주어진다.
                다음 N개의 줄에 보드의 모양을 나타내는 길이 M의 문자열이 주어진다.
                이 문자열은 '.', '#', 'O', 'R', 'B' 로 이루어져 있다.
                '.'은 빈 칸을 의미하고, '#'은 공이 이동할 수 없는 장애물 또는 벽을 의미하며,
                'O'는 구멍의 위치를 의미한다.
                'R'은 빨간 구슬의 위치, 'B'는 파란 구슬의 위치이다.

                입력되는 모든 보드의 가장자리에는 모두 '#'이 있다.
                구멍의 개수는 한 개이며, 빨간 구슬과 파란 구슬은 항상 1개가 주어진다.


                [출력]

                최소 몇 번 만에 빨간 구슬을 구멍을 통해 빼낼 수 있는지 출력한다.
                만약, 10번 이하로 움직여서 빨간 구슬을 구멍을 통해 빼낼 수 없으면 -1을 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("구슬 탈출")
                .difficulty(Difficulty.HARD)
                .description(description)
                .tags("simulation,bfs,implementation")
                .build();

        problemRepository.save(problem);
    }

    // 12. 마법사 상어와 복제
    private void createSharkCopyMagicProblem() {
        if (problemRepository.existsByTitle("마법사 상어와 복제")) {
            return;
        }

        String description = """
                [문제]

                마법사 상어는 파이어볼, 토네이도, 파이어스톰, 물복사버그, 비바라기, 블리자드 마법을 할 수 있다.
                오늘은 기존에 배운 물복사버그 마법의 상위 마법인 복제를 배웠고,
                4 × 4 크기의 격자에서 연습하려고 한다.
                (r, c)는 격자의 r행 c열을 의미한다.
                격자의 가장 왼쪽 윗 칸은 (1, 1)이고, 가장 오른쪽 아랫 칸은 (4, 4)이다.

                격자에는 물고기 M마리가 있다.
                각 물고기는 격자의 칸 하나에 들어가 있으며, 이동 방향을 가지고 있다.
                이동 방향은 8가지 방향(상하좌우, 대각선) 중 하나이다.
                마법사 상어도 연습을 위해 격자에 들어가있다.
                상어도 격자의 한 칸에 들어가있다.
                둘 이상의 물고기가 같은 칸에 있을 수도 있으며,
                마법사 상어와 물고기가 같은 칸에 있을 수도 있다.

                상어의 마법 연습 한 번은 다음과 같은 작업이 순차적으로 이루어진다.

                1. 상어가 모든 물고기에게 복제 마법을 시전한다.
                   복제 마법은 시간이 조금 걸리기 때문에, 아래 5번에서 물고기가 복제되어 나타난다.

                2. 모든 물고기가 한 칸 이동한다.
                   상어가 있는 칸, 물고기의 냄새가 있는 칸, 격자의 범위를 벗어나는 칸으로는 이동할 수 없다.
                   각 물고기는 자신이 가지고 있는 이동 방향이 이동할 수 있는 칸을 향할 때까지
                   방향을 45도 반시계 회전시킨다.
                   이동할 수 있는 칸이 없으면 이동하지 않는다.

                3. 상어가 연속해서 3칸 이동한다.
                   상어는 상하좌우로 인접한 칸으로 이동할 수 있다.
                   이동 중 격자를 벗어나면 그 방법은 불가능하다.
                   이동 중 물고기가 있는 칸에 도착하면, 그 칸의 모든 물고기는 제거되고 냄새를 남긴다.
                   가능한 이동 방법 중 제거되는 물고기가 가장 많은 방법을 선택하며,
                   동일하다면 사전순으로 가장 앞서는 방법을 선택한다.

                4. 두 번 전 연습에서 생긴 물고기의 냄새가 격자에서 사라진다.

                5. 1에서 사용된 복제 마법이 완료되어 복제된 물고기가 생성된다.


                [입력]

                첫째 줄에 물고기의 수 M, 연습 횟수 S가 주어진다.
                다음 M개의 줄에는 물고기의 정보 (fx, fy, d)가 주어지며,
                d는 1~8 방향을 의미한다. (←, ↖, ↑, ↗, →, ↘, ↓, ↙)

                마지막 줄에는 상어의 위치 (sx, sy)가 주어진다.

                격자 위에 있는 물고기의 수가 항상 1,000,000 이하인 입력만 주어진다.


                [출력]

                S번의 연습을 마친 후 격자에 있는 물고기의 수를 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("마법사 상어와 복제")
                .difficulty(Difficulty.HARD)
                .description(description)
                .tags("simulation,backtracking,implementation")
                .build();

        problemRepository.save(problem);
    }

    // 13. 비슷한 단어
    private void createSimilarWordsProblem() {
        if (problemRepository.existsByTitle("비슷한 단어")) {
            return;
        }

        String description = """
                [문제]

                N개의 영단어들이 주어졌을 때, 가장 비슷한 두 단어를 구해내는 프로그램을 작성하시오.

                두 단어의 비슷한 정도는 두 단어의 접두사의 길이로 측정한다.
                접두사란 두 단어의 앞부분에서 공통적으로 나타나는 부분문자열을 말한다.
                즉, 두 단어의 앞에서부터 M개의 글자들이 같으면서 M이 최대인 경우를 구하는 것이다.
                "AHEHHEH", "AHAHEH"의 접두사는 "AH"가 되고, "AB", "CD"의 접두사는 ""(길이가 0)이 된다.

                접두사의 길이가 최대인 경우가 여러 개일 때에는 입력되는 순서대로 제일 앞쪽에 있는 단어를 답으로 한다.
                즉, 답으로 S라는 문자열과 T라는 문자열을 출력한다고 했을 때,
                우선 S가 입력되는 순서대로 제일 앞쪽에 있는 단어인 경우를 출력하고,
                그런 경우도 여러 개 있을 때에는 그 중에서 T가 입력되는 순서대로 제일 앞쪽에 있는 단어인 경우를 출력한다.


                [입력]

                첫째 줄에 N(2 ≤ N ≤ 20,000)이 주어진다.
                다음 N개의 줄에 알파벳 소문자로만 이루어진 길이 100자 이하의 서로 다른 영단어가 주어진다.


                [출력]

                첫째 줄에 S를, 둘째 줄에 T를 출력한다.
                단, 이 두 단어는 서로 달라야 한다. 즉, 가장 비슷한 두 단어를 구할 때 같은 단어는 제외하는 것이다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("비슷한 단어")
                .difficulty(Difficulty.HARD)
                .description(description)
                .tags("string,sorting")
                .build();

        problemRepository.save(problem);
    }

    // 14. 보석 도둑
    private void createJewelThiefProblem() {
        if (problemRepository.existsByTitle("보석 도둑")) {
            return;
        }

        String description = """
                [문제]

                세계적인 도둑 상덕이는 보석점을 털기로 결심했다.
                상덕이가 털 보석점에는 보석이 총 N개 있다. 각 보석은 무게 Mi와 가격 Vi를 가지고 있다.
                상덕이는 가방을 K개 가지고 있고, 각 가방에 담을 수 있는 최대 무게는 Ci이다.
                가방에는 최대 한 개의 보석만 넣을 수 있다.
                상덕이가 훔칠 수 있는 보석의 최대 가격을 구하는 프로그램을 작성하시오.


                [입력]

                첫째 줄에 N과 K가 주어진다. (1 ≤ N, K ≤ 300,000)
                다음 N개 줄에는 각 보석의 정보 Mi와 Vi가 주어진다. (0 ≤ Mi, Vi ≤ 1,000,000)
                다음 K개 줄에는 가방에 담을 수 있는 최대 무게 Ci가 주어진다. (1 ≤ Ci ≤ 100,000,000)
                모든 숫자는 양의 정수이다.


                [출력]

                첫째 줄에 상덕이가 훔칠 수 있는 보석 가격의 합의 최댓값을 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("보석 도둑")
                .difficulty(Difficulty.HARD)
                .description(description)
                .tags("greedy,sorting,priority_queue")
                .build();

        problemRepository.save(problem);
    }

    // 15. 화성 탐사 (New)
    private void createMarsExplorationProblem() {
        if (problemRepository.existsByTitle("화성 탐사")) {
            return;
        }

        String description = """
                [문제]

                NASA에서는 화성 탐사를 위해 화성에 무선 조종 로봇을 보냈다. 실제 화성의 모습은 굉장히 복잡하지만,
                로봇의 메모리가 얼마 안 되기 때문에 지형을 N×M 배열로 단순화 하여 생각하기로 한다.
                지형의 고저차의 특성상, 로봇은 움직일 때 배열에서 왼쪽, 오른쪽, 아래쪽으로 이동할 수 있지만, 위쪽으로는 이동할 수 없다.
                또한 한 번 탐사한 지역(배열에서 하나의 칸)은 탐사하지 않기로 한다.

                각각의 지역은 탐사 가치가 있는데, 로봇을 배열의 왼쪽 위 (1, 1)에서 출발시켜 오른쪽 아래 (N, M)으로 보내려고 한다.
                이때, 위의 조건을 만족하면서, 탐사한 지역들의 가치의 합이 최대가 되도록 하는 프로그램을 작성하시오.


                [입력]

                첫째 줄에 N, M(1≤N, M≤1,000)이 주어진다. 다음 N개의 줄에는 M개의 수로 배열이 주어진다.
                배열의 각 수는 절댓값이 100을 넘지 않는 정수이다. 이 값은 그 지역의 가치를 나타낸다.


                [출력]

                첫째 줄에 최대 가치의 합을 출력한다.
                """;

        CodingProblem problem = CodingProblem.builder()
                .title("화성 탐사")
                .difficulty(Difficulty.HARD)
                .description(description)
                .tags("dp")
                .build();

        problemRepository.save(problem);
    }
}