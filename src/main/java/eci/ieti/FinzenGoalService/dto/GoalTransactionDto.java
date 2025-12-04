package eci.ieti.FinzenGoalService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoalTransactionDto {
    private Long id;
    private Long goalId;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDateTime date;
}