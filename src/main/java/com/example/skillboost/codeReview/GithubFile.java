package com.example.skillboost.codeReview;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GithubFile {
    private String path;
    private String content;

    public GithubFile(String path, String content) {
        this.path = path;
        this.content = content;
    }
}
