package com.example.skillboost.codingtest.judge;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JudgeClient {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final int TIMEOUT_SECONDS = 2; // 시간 제한

    /**
     * CodingTestService에서 호출하는 메서드
     * 소스코드, 언어, 입력값을 받아 실행 결과를 반환
     */
    public JudgeResult execute(String sourceCode, String language, String input) {
        String uniqueId = UUID.randomUUID().toString();
        File sourceFile = createSourceFile(language, sourceCode, uniqueId);

        if (sourceFile == null) {
            return JudgeResult.runtimeError("Internal Error: 파일 생성 실패");
        }

        try {
            // 1. 컴파일 (Java, C++ 만)
            if (language.equalsIgnoreCase("java") || language.equalsIgnoreCase("cpp")) {
                String compileError = compileCode(language, sourceFile);
                if (compileError != null) {
                    return JudgeResult.compileError(compileError);
                }
            }

            // 2. 실행
            return runCode(language, sourceFile, input);

        } catch (Exception e) {
            return JudgeResult.runtimeError(e.getMessage());
        } finally {
            cleanup(sourceFile);
        }
    }

    // --- 내부 헬퍼 메서드 ---

    private File createSourceFile(String language, String code, String uniqueId) {
        try {
            String fileName;
            // 언어별 파일 확장자 및 클래스명 처리
            if (language.equalsIgnoreCase("java")) {
                fileName = "Main.java"; // Java는 Main 클래스 강제
            } else if (language.equalsIgnoreCase("cpp")) {
                fileName = uniqueId + ".cpp";
            } else { // python
                fileName = uniqueId + ".py";
            }

            // 폴더 분리 (동시 실행 충돌 방지)
            Path dirPath = Path.of(TEMP_DIR, "judge_" + uniqueId);
            Files.createDirectories(dirPath);

            File file = dirPath.resolve(fileName).toFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(code);
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String compileCode(String language, File sourceFile) {
        ProcessBuilder pb;
        if (language.equalsIgnoreCase("java")) {
            // javac -encoding UTF-8 Main.java
            pb = new ProcessBuilder("javac", "-encoding", "UTF-8", sourceFile.getAbsolutePath());
        } else {
            // g++ -o output source.cpp
            String outputPath = sourceFile.getParent() + File.separator + "output";
            // Windows인 경우 .exe 붙임
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                outputPath += ".exe";
            }
            pb = new ProcessBuilder("g++", "-o", outputPath, sourceFile.getAbsolutePath());
        }

        pb.directory(sourceFile.getParentFile());
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return "Time Limit Exceeded during Compilation";
            }
            if (process.exitValue() != 0) {
                return readProcessOutput(process.getInputStream());
            }
            return null; // 컴파일 성공
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private JudgeResult runCode(String language, File sourceFile, String input) {
        ProcessBuilder pb;
        long startTime = System.currentTimeMillis();

        try {
            if (language.equalsIgnoreCase("java")) {
                pb = new ProcessBuilder("java", "-cp", ".", "Main");
            } else if (language.equalsIgnoreCase("python")) {
                pb = new ProcessBuilder("python", sourceFile.getName()); // python3 라면 "python3"
            } else { // cpp
                String cmd = System.getProperty("os.name").toLowerCase().contains("win") ? "output.exe" : "./output";
                pb = new ProcessBuilder(cmd);
            }

            pb.directory(sourceFile.getParentFile());
            Process process = pb.start();

            // 입력값 주입
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(input);
                writer.flush();
            }

            // 실행 대기
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return JudgeResult.builder().statusId(5).message("Time Limit Exceeded").build();
            }

            // 결과 읽기
            String output = readProcessOutput(process.getInputStream());
            String error = readProcessOutput(process.getErrorStream());
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;

            if (process.exitValue() != 0) {
                return JudgeResult.runtimeError(error.isEmpty() ? "Runtime Error" : error);
            }

            // 로컬 실행 성공 (정답 여부는 Service에서 판단하므로 여기선 성공 상태 리턴)
            // JudgeResult.accepted()는 statusId=3을 반환하여 Service가 정답 비교를 진행하게 함
            return JudgeResult.accepted(output, duration);

        } catch (Exception e) {
            return JudgeResult.runtimeError(e.getMessage());
        }
    }

    private String readProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private void cleanup(File sourceFile) {
        try {
            if (sourceFile == null) return;
            File dir = sourceFile.getParentFile();
            if (dir != null && dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) f.delete();
                }
                dir.delete();
            }
        } catch (Exception e) {
            // ignore
        }
    }
}