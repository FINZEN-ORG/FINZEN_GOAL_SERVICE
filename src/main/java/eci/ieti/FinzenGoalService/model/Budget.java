package eci.ieti.FinzenGoalService.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "budgets", indexes = {
        @Index(columnList = "user_id, category", name = "idx_user_category")
})
@Getter
@Setter
@NoArgsConstructor
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal; // optional link to a goal
    @Column(nullable = false)
    private String category; // textual category aligned with Transactions categories
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal initialAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}