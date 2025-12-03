package eci.ieti.FinzenGoalService.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_transactions")
@Getter
@Setter
@NoArgsConstructor
public class GoalTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long goalId;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private String type; // "DEPOSIT" o "WITHDRAW"
    private String description;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime date;

    public GoalTransaction(Long goalId, BigDecimal amount, String type, String description) {
        this.goalId = goalId;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }
}