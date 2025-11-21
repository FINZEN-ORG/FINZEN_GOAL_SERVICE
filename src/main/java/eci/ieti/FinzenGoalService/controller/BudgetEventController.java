package eci.ieti.FinzenGoalService.controller;

import eci.ieti.FinzenGoalService.dto.BudgetExpenseEventDto;
import eci.ieti.FinzenGoalService.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetEventController {

    private final BudgetService budgetService;

    @PostMapping("/update-on-expense")
    public ResponseEntity<Void> updateBudgetOnExpense(@RequestBody BudgetExpenseEventDto dto) {
        budgetService.updateOnExpense(dto);
        return ResponseEntity.ok().build();
    }
}