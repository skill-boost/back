package com.example.skillboost.codereview.dto;

import java.util.ArrayList;
import java.util.List;

public class CodeReviewResponse {

    private String review;
    private List<String> questions = new ArrayList<>();

    public CodeReviewResponse() {
    }

    public CodeReviewResponse(String review, List<String> questions) {
        this.review = review;
        this.questions = questions;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }
}
