package eci.ieti.FinzenGoalService.repository;

import eci.ieti.FinzenGoalService.model.GoalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalTransactionRepository extends JpaRepository<GoalTransaction, Long> {
    List<GoalTransaction> findByGoalIdOrderByDateDesc(Long goalId);
}