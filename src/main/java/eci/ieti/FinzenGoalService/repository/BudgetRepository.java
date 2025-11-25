package eci.ieti.FinzenGoalService.repository;

import eci.ieti.FinzenGoalService.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserId(Long userId);
    Optional<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);
}