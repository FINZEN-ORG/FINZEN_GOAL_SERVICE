package eci.ieti.FinzenGoalService.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eci.ieti.FinzenGoalService.dto.request.GoalRequestDto;
import eci.ieti.FinzenGoalService.dto.response.GoalResponseDto;
import eci.ieti.FinzenGoalService.mapper.GoalMapper;
import eci.ieti.FinzenGoalService.repository.GoalRepository;
import eci.ieti.FinzenGoalService.service.GoalService;

@Service
public class GoalServiceimpl implements GoalService {

    @Autowired
    GoalRepository goalRepository;
    @Autowired
    GoalMapper goalMapper;

    public GoalResponseDto createGoal(GoalRequestDto dto) {
        if (goalRepository.existsById(dto.getName())) {
            // Agregar manejo de error xd
            return null;
        }
        goalRepository.save(goalMapper.toEntity(dto));

        return goalMapper.toDTO(goalMapper.toEntity(dto));
    }

    public List<GoalResponseDto> getAllGoals() {
        return goalMapper.toDTOList(goalRepository.findAll());
    }

    @Override
    public GoalResponseDto getGoalByName(String name) {
        return goalMapper.toDTO(goalRepository.findById(name).orElse(null)); // Agregar manejo de error xd

    }

    @Override
    public GoalResponseDto updateGoal(GoalRequestDto dto, String name) {
        if (!goalRepository.existsById(name)) {
            return null; // Agregar manejo de error xd
        }
        goalRepository.save(goalMapper.toEntity(dto));
        return goalMapper.toDTO(goalMapper.toEntity(dto));

    }

    @Override
    public void deleteGoal(String name) {
        if (goalRepository.existsById(name)) {
            goalRepository.deleteById(name);
        }
        // Agregar manejo de error xd
    }
    
}
