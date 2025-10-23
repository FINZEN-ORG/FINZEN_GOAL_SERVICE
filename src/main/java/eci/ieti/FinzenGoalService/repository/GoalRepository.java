package eci.ieti.FinzenGoalService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eci.ieti.FinzenGoalService.model.Goal;

public interface GoalRepository extends JpaRepository<Goal, String> {

}