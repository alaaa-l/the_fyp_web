package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ArticleRepository;
import com.capstone.OpportuGrow.model.Article;
import com.capstone.OpportuGrow.model.ArticleType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class ArticleController {
    private ArticleRepository articleRepository;

    public ArticleController(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @GetMapping("/articles")
    public String viewArticles(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Article> articles;

        if ((type != null && !type.isEmpty()) && (keyword != null && !keyword.isEmpty())) {
            articles = articleRepository.findByTypeAndTitleContainingIgnoreCaseAndPublishedTrueOrderByUploadedAtDesc(
                    ArticleType.valueOf(type), keyword);
        } else if (type != null && !type.isEmpty()) {
            articles = articleRepository.findByTypeAndPublishedTrueOrderByUploadedAtDesc(ArticleType.valueOf(type));
        } else if (keyword != null && !keyword.isEmpty()) {
            articles = articleRepository.findByTitleContainingIgnoreCaseAndPublishedTrueOrderByUploadedAtDesc(keyword);
        } else {
            articles = articleRepository.findAllByPublishedTrueOrderByUploadedAtDesc();
        }

        model.addAttribute("articles", articles);
        model.addAttribute("selectedType", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("types", ArticleType.values());
        return "user-articles";
    }

    @GetMapping("/member/articles/{id}")
    public String viewArticleDetails(@PathVariable Long id, Model model) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid article Id:" + id));
        model.addAttribute("article", article);
        return "article-details";
    }

    @GetMapping("/member/articles/{id}/download")
    public ResponseEntity<Resource> downloadArticle(@PathVariable Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid article Id:" + id));

        if (article.getType() == ArticleType.LINK || article.getType() == ArticleType.VIDEO) {
            // It's a link, should have been handled by the frontend, but as a fallback
            // redirect
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, article.getFileUrl()).build();
        }

        try {
            // The fileURL is saved as "/uploads/articles/filename"
            String fileName = article.getFileUrl().replace("/uploads/articles/", "");
            Path filePath = Paths.get("C:/og-uploads/articles/").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
