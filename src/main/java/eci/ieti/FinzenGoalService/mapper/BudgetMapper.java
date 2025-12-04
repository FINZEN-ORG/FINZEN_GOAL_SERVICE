package eci.ieti.FinzenGoalService.mapper;

import eci.ieti.FinzenGoalService.dto.BudgetDto;
import eci.ieti.FinzenGoalService.model.Budget;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    BudgetDto toDto(Budget entity);
    Budget toEntity(BudgetDto dto);
}