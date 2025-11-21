package eci.ieti.FinzenGoalService.controller;

import eci.ieti.FinzenGoalService.dto.BudgetDto;
import eci.ieti.FinzenGoalService.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetDto> createOrUpdate(@Valid @RequestBody BudgetDto dto, Authentication authentication) {
        dto.setUserId(Long.valueOf(authentication.getName()));
        return ResponseEntity.ok(budgetService.createOrUpdate(dto));
    }

    @GetMapping
    public ResponseEntity<List<BudgetDto>> list(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(budgetService.listByUser(userId));
    }
}