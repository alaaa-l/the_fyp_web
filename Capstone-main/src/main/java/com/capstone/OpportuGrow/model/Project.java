package com.capstone.OpportuGrow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;
    @ManyToMany
    @JoinTable(name = "project_likes", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> likedUsers = new HashSet<>();

    public Set<User> getLikedUsers() {
        return likedUsers;
    }

    public void setLikedUsers(Set<User> likedUsers) {
        this.likedUsers = likedUsers;
    }

    // زيد هاي فوق مع الـ Attributes
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    // وزيد الـ Getter والـ Setter تحت
    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Transient
    private boolean likedByCurrentUser;

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    private String title;
    @Column(columnDefinition = "TEXT")
    private String shortDescription;
    @Column(columnDefinition = "TEXT", nullable = true)
    private String longDescription;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String projectStory;
    private String category;
    private Double fundingGoal;
    private Integer fundingDuration;
    private String imageUrl;
    private BigDecimal platformFee;
    private BigDecimal processingFee;
    private BigDecimal amountYouWillReceive;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    private Role role;
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String address;

    private boolean urgent;

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public BigDecimal getAmountYouWillReceive() {
        return amountYouWillReceive;
    }

    public void setAmountYouWillReceive(BigDecimal amountYouWillReceive) {
        this.amountYouWillReceive = amountYouWillReceive;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;
    @ElementCollection
    private List<String> galleryUrls;

    private String videoUrl;
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;
    private Double raisedAmount = 0.0;
    @ManyToOne
    @JoinColumn(name = "consultant_id")
    private Consultant consultant;
    @Transient // ما نخزنها بالـ DB
    private double fundingPercent;

    // مثال حساب fundingPercent قبل الإرسال للـ template
    public void calculateFundingPercent() {
        if (this.fundingGoal > 0) {
            this.fundingPercent = (this.raisedAmount / this.fundingGoal) * 100;
        } else {
            this.fundingPercent = 0;
        }
    }

    public double getFundingPercent() {
        return fundingPercent;
    }

    public void setFundingPercent(double fundingPercent) {
        this.fundingPercent = fundingPercent;
    }

    public Consultant getConsultant() {
        return consultant;
    }

    public void setConsultant(Consultant consultant) {
        this.consultant = consultant;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Double getRaisedAmount() {
        return raisedAmount;
    }

    public void setRaisedAmount(Double raisedAmount) {
        this.raisedAmount = raisedAmount;
    }

    public List<String> getGalleryUrls() {
        return galleryUrls;
    }

    public void setGalleryUrls(List<String> galleryUrls) {
        this.galleryUrls = galleryUrls;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public ProjectType getType() {
        return type;
    }

    public String getProjectStory() {
        return projectStory;
    }

    public void setProjectStory(String projectStory) {
        this.projectStory = projectStory;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    @Transient
    public String getOwnerFullName() {
        return owner != null ? owner.getName() : "Unknown";
    }

    @Transient
    public int getLikesCount() {
        return likedUsers != null ? likedUsers.size() : 0;
    }

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getFundingGoal() {
        return fundingGoal;
    }

    public void setFundingGoal(Double fundingGoal) {
        this.fundingGoal = fundingGoal;
    }

    public Integer getFundingDuration() {
        return fundingDuration;
    }

    public void setFundingDuration(Integer fundingDuration) {
        this.fundingDuration = fundingDuration;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

}
