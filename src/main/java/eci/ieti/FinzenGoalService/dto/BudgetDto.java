package eci.ieti.FinzenGoalService.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetDto {
    private Long id;
    private Long userId;
    private Long goalId; // optional
    private String category;
    private BigDecimal amount;
    private BigDecimal initialAmount;
    private LocalDate startDate;
    private LocalDate endDate;
}