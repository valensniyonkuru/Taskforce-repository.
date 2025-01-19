package com.taskforce.wallet_app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Budget name is required")
    @Size(min = 1, max = 100, message = "Budget name must be between 1 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Budget limit is required")
    @Positive(message = "Budget limit must be greater than zero")
    @Column(name = "budget_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetLimit;

    @Column(name = "spent_amount", precision = 10, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "subCategories", "parentCategory"})
    private Category category;

    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Column(name = "notification_threshold")
    private Integer notificationThreshold;

    @Column(name = "is_active")
    private boolean active = true;

    @Size(max = 500, message = "Notes cannot be longer than 500 characters")
    @Column(length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        if (spentAmount == null) {
            spentAmount = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount != null ? spentAmount : BigDecimal.ZERO;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public Integer getNotificationThreshold() {
        return notificationThreshold;
    }

    public void setNotificationThreshold(Integer notificationThreshold) {
        this.notificationThreshold = notificationThreshold;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes.trim() : null;
    }

    // Helper Methods
    public boolean isExceeded() {
        return spentAmount != null && budgetLimit != null && 
               spentAmount.compareTo(budgetLimit) > 0;
    }

    public BigDecimal getRemainingAmount() {
        if (budgetLimit == null || spentAmount == null) {
            return BigDecimal.ZERO;
        }
        return budgetLimit.subtract(spentAmount);
    }

    public double getPercentageSpent() {
        if (budgetLimit == null || budgetLimit.compareTo(BigDecimal.ZERO) == 0 || 
            spentAmount == null) {
            return 0.0;
        }
        return spentAmount.divide(budgetLimit, 4, java.math.RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .doubleValue();
    }

    public boolean isWithinThreshold() {
        if (notificationThreshold == null || budgetLimit == null || spentAmount == null) {
            return false;
        }
        BigDecimal thresholdAmount = budgetLimit.multiply(
            new BigDecimal(notificationThreshold).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP)
        );
        return spentAmount.compareTo(thresholdAmount) >= 0;
    }
}
