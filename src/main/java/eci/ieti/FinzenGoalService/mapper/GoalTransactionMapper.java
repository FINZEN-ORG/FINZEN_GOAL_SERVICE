package eci.ieti.FinzenGoalService.mapper;

import eci.ieti.FinzenGoalService.dto.GoalTransactionDto;
import eci.ieti.FinzenGoalService.model.GoalTransaction;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalTransactionMapper {
    GoalTransactionDto toDto(GoalTransaction entity);
    List<GoalTransactionDto> toDtoList(List<GoalTransaction> entities);
}