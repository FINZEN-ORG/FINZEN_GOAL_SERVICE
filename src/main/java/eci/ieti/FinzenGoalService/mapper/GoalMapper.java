package eci.ieti.FinzenGoalService.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import eci.ieti.FinzenGoalService.dto.request.GoalRequestDto;
import eci.ieti.FinzenGoalService.dto.response.GoalResponseDto;
import eci.ieti.FinzenGoalService.model.Goal;

@Mapper(componentModel = "spring") // <- permite inyectarlo con @Autowired
public interface GoalMapper {

    // Mapea campos con el mismo nombre automáticamente
    GoalResponseDto toDTO(Goal entity);

    Goal toEntity(GoalRequestDto dto);

    List<GoalResponseDto> toDTOList(List<Goal> all);

    
}

