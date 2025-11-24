package eci.ieti.FinzenGoalService.dto;

import eci.ieti.FinzenGoalService.model.GoalCategory;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalDto {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private LocalDate dueDate;
    private GoalCategory category;
    private String status;
}