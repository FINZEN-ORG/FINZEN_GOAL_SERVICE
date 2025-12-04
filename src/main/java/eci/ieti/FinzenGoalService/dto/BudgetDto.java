package eci.ieti.FinzenGoalService.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetDto {
    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal amount; // Límite
    private BigDecimal spent;  // Este campo se llenará con datos de TransactionService en tiempo de ejecución
    private LocalDate startDate;
    private LocalDate endDate;
}