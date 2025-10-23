package eci.ieti.FinzenGoalService.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponseDto {
    private String name;
    private int targetAmount;
    private LocalDate deadline;
}
