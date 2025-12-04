package eci.ieti.FinzenGoalService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDto {
    private Long categoryId;
    private BigDecimal totalAmount;
}