package com.capstone.OpportuGrow.Dto;

import com.capstone.OpportuGrow.model.ArticleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private Long id;
    private String title;
    private String description;
    private ArticleType type;
    private String consultantName;
    private LocalDate uploadedAt;
    private String fileUrl;
    private String category;
    private String content;
}
