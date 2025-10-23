package eci.ieti.FinzenGoalService.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequestDto {
    private String name;
    private int targetAmount;
    private LocalDate deadline;
}
