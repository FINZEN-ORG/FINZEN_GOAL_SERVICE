package eci.ieti.FinzenGoalService.mapper;

import eci.ieti.FinzenGoalService.dto.GoalDto;
import eci.ieti.FinzenGoalService.model.Goal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GoalMapper {
    GoalDto toDto(Goal entity);
    Goal toEntity(GoalDto dto);
}