// src/main/java/com/example/skillboost/codereview/dto/CodeReviewRequest.java
package com.example.skillboost.codereview.dto;

public class CodeReviewRequest {

    private String code;
    private String comment;

    // 🔹 레포지터리 기반 리뷰용 필드
    private String repoUrl;   // 예: https://github.com/Junseung-Ock/java-calculator-7
    private String branch;    // 기본값: main

    public CodeReviewRequest() {
    }

    public CodeReviewRequest(String code, String comment) {
        this.code = code;
        this.comment = comment;
    }

    public CodeReviewRequest(String code, String comment, String repoUrl, String branch) {
        this.code = code;
        this.comment = comment;
        this.repoUrl = repoUrl;
        this.branch = branch;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
