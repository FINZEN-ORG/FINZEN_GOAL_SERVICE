package eci.ieti.FinzenGoalService.service;

import eci.ieti.FinzenGoalService.dto.GoalDto;
import eci.ieti.FinzenGoalService.mapper.GoalMapper;
import eci.ieti.FinzenGoalService.model.Goal;
import eci.ieti.FinzenGoalService.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final GoalMapper mapper;

    public GoalDto create(GoalDto dto) {
        Goal entity = mapper.toEntity(dto);
        entity.setSavedAmount(dto.getSavedAmount() == null ? entity.getSavedAmount() : dto.getSavedAmount());
        entity.setStatus(entity.getStatus() == null ? "ACTIVE" : entity.getStatus());
        Goal saved = goalRepository.save(entity);
        return mapper.toDto(saved);
    }

    public List<GoalDto> listByUser(Long userId) {
        return goalRepository.findByUserId(userId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public GoalDto getById(Long id) {
        return goalRepository.findById(id).map(mapper::toDto).orElseThrow();
    }
}