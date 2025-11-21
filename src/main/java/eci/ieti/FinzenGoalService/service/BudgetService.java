package eci.ieti.FinzenGoalService.service;

import eci.ieti.FinzenGoalService.dto.BudgetDto;
import eci.ieti.FinzenGoalService.dto.BudgetExpenseEventDto;
import eci.ieti.FinzenGoalService.mapper.BudgetMapper;
import eci.ieti.FinzenGoalService.model.Budget;
import eci.ieti.FinzenGoalService.model.Goal;
import eci.ieti.FinzenGoalService.repository.BudgetRepository;
import eci.ieti.FinzenGoalService.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetMapper mapper;
    private final GoalRepository goalRepository;

    public BudgetDto createOrUpdate(BudgetDto dto) {
        Budget budget = mapper.toEntity(dto);
        if (dto.getGoalId() != null) {
            Goal goal = goalRepository.findById(dto.getGoalId()).orElseThrow();
            budget.setGoal(goal);
        }
        Budget saved = budgetRepository.save(budget);
        return mapper.toDto(saved);
    }

    public List<BudgetDto> listByUser(Long userId) {
        return budgetRepository.findByUserId(userId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public void updateOnExpense(BudgetExpenseEventDto dto) {
        Long userId = dto.getUserId();
        String categoryName = dto.getCategory();
        BigDecimal amount = dto.getAmount();

        // Buscar Budget por userId + category
        Budget budget = budgetRepository.findByUserIdAndCategory(userId, categoryName)
                .orElse(null);

        // Si no existe, ignorar
        if (budget == null) {
            return;
        }

        // Acción
        if ("created".equalsIgnoreCase(dto.getAction())) {
            budget.setSpent(budget.getSpent().add(amount));
        } else if ("deleted".equalsIgnoreCase(dto.getAction())) {
            budget.setSpent(budget.getSpent().subtract(amount));
        }

        budgetRepository.save(budget);
    }
}