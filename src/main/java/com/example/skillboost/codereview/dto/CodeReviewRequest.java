package com.example.skillboost.codereview.dto;

public class CodeReviewRequest {

    private String code;
    private String comment;

    public CodeReviewRequest() {
    }

    public CodeReviewRequest(String code, String comment) {
        this.code = code;
        this.comment = comment;
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
}
