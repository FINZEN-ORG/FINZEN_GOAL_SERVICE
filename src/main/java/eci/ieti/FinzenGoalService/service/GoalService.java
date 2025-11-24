package eci.ieti.FinzenGoalService.service;

import eci.ieti.FinzenGoalService.dto.GoalDto;
import eci.ieti.FinzenGoalService.mapper.GoalMapper;
import eci.ieti.FinzenGoalService.model.Goal;
import eci.ieti.FinzenGoalService.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
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

    @Transactional
    public GoalDto deposit(Long goalId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        // Sumar al saldo actual
        goal.setSavedAmount(goal.getSavedAmount().add(amount));

        // Verificar si se completó la meta
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("COMPLETED");
        }

        Goal saved = goalRepository.save(goal);
        return mapper.toDto(saved);
    }

    @Transactional
    public GoalDto withdraw(Long goalId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (goal.getSavedAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in this goal");
        }

        goal.setSavedAmount(goal.getSavedAmount().subtract(amount));

        // Si estaba completada y sacamos dinero, vuelve a estar activa
        if ("COMPLETED".equals(goal.getStatus()) && goal.getSavedAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus("ACTIVE");
        }

        Goal saved = goalRepository.save(goal);
        return mapper.toDto(saved);
    }
}