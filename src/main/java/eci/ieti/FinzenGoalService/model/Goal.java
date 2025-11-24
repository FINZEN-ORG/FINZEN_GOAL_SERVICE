package eci.ieti.FinzenGoalService.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "text")
    private String description;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal targetAmount;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal savedAmount = BigDecimal.ZERO;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategory category;
    @Column(nullable = false)
    private String status; // ACTIVE, COMPLETED, EXPIRED
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}