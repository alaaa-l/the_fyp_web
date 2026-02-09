package com.capstone.OpportuGrow.Dto;

import com.capstone.OpportuGrow.model.ProjectStatus;
import com.capstone.OpportuGrow.model.ProjectType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ProjectResponseDto {
    private Long id;
    private String title;
    private String shortDescription;
    private String projectStory;
    private String category;
    private Double fundingGoal;
    private Integer fundingDuration;
    private Double currentFunding;
    private ProjectStatus status;
    private ProjectType type;
    private String imageUrl;
    private String ownerName;
    private Date createdAt;
    private double fundingPercent;
    private Long raisedAmount;
    private boolean urgent;
}
