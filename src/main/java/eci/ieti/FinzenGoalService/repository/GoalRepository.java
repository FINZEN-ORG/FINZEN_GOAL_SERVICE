package eci.ieti.FinzenGoalService.repository;

import eci.ieti.FinzenGoalService.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserId(Long userId);
}