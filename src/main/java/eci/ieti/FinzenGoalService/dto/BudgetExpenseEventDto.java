package eci.ieti.FinzenGoalService.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class BudgetExpenseEventDto {
    private Long userId;
    private String category;
    private BigDecimal amount;
    private String action; // "created" | "deleted"
}