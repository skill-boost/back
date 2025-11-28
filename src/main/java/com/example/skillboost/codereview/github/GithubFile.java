// src/main/java/com/example/skillboost/codereview/github/GithubFile.java
package com.example.skillboost.codereview.github;

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
