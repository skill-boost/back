package com.example.skillboost.codeReview.controller;

import com.example.skillboost.codeReview.GithubFile;
import com.example.skillboost.codeReview.service.GithubService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final GithubService githubService;

    public GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/repo")
    public List<GithubFile> getRepoContents(
            @RequestParam String repoUrl,
            @RequestParam(defaultValue = "main") String branch
    ) {
        return githubService.fetchRepoCode(repoUrl, branch);
    }
}
