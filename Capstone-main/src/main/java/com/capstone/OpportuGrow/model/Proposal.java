package com.capstone.OpportuGrow.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "proposals")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    private Double myBudget;

    private Double estimatedBudget;

    @Column(columnDefinition = "TEXT")
    private String idea;

    @Column(columnDefinition = "TEXT")
    private String contactInfo;

    private String category;

    private String location;

    private boolean canBeOnline;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ProposalStatus status = ProposalStatus.PENDING;
}
