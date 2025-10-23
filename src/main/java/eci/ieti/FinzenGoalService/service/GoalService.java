package eci.ieti.FinzenGoalService.service;

import java.util.List;

import eci.ieti.FinzenGoalService.dto.request.GoalRequestDto;
import eci.ieti.FinzenGoalService.dto.response.GoalResponseDto;

public interface GoalService {
    GoalResponseDto createGoal(GoalRequestDto dto);
    List<GoalResponseDto> getAllGoals();
    GoalResponseDto getGoalByName(String name);
    GoalResponseDto updateGoal(GoalRequestDto dto, String name);
    void deleteGoal(String name);
}
