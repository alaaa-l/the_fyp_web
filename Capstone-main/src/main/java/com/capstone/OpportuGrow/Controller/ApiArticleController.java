package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.ArticleDto;
import com.capstone.OpportuGrow.Repository.ArticleRepository;
import com.capstone.OpportuGrow.model.Article;
import com.capstone.OpportuGrow.model.ArticleType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
public class ApiArticleController {

    private final ArticleRepository articleRepository;

    public ApiArticleController(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @GetMapping
    public ResponseEntity<List<ArticleDto>> getArticles(
            @RequestParam(required = false) ArticleType type,
            @RequestParam(required = false) String keyword) {

        List<Article> articles;

        if (type != null && keyword != null && !keyword.isEmpty()) {
            articles = articleRepository
                    .findByTypeAndTitleContainingIgnoreCaseAndPublishedTrueOrderByUploadedAtDesc(type, keyword);
        } else if (type != null) {
            articles = articleRepository.findByTypeAndPublishedTrueOrderByUploadedAtDesc(type);
        } else if (keyword != null && !keyword.isEmpty()) {
            articles = articleRepository.findByTitleContainingIgnoreCaseAndPublishedTrueOrderByUploadedAtDesc(keyword);
        } else {
            articles = articleRepository.findAllByPublishedTrueOrderByUploadedAtDesc();
        }

        List<ArticleDto> dtos = articles.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private ArticleDto mapToDto(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .type(article.getType())
                .consultantName(article.getConsultant() != null
                        ? article.getConsultant().getName()
                        : "Unknown")
                .uploadedAt(article.getUploadedAt())
                .fileUrl(article.getFileUrl())
                .category(article.getCategory())
                .content(article.getContent())
                .build();
    }
}
