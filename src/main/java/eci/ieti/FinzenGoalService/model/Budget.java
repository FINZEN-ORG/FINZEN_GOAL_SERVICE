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
        @Index(columnList = "userId, categoryId", name = "idx_user_category")
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
    @Column(nullable = false)
    private Long categoryId;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount; // El límite del presupuesto
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}