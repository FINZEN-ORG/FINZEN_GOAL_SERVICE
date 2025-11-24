package eci.ieti.FinzenGoalService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TransactionSummaryDto {
    private Long categoryId;
    private BigDecimal totalAmount;
}