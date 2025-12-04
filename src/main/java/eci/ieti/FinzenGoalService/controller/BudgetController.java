package eci.ieti.FinzenGoalService.controller;

import eci.ieti.FinzenGoalService.dto.BudgetDto;
import eci.ieti.FinzenGoalService.service.BudgetService;
import jakarta.servlet.http.HttpServletRequest; // Importante
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
    public ResponseEntity<List<BudgetDto>> list(Authentication authentication, HttpServletRequest request) {
        Long userId = Long.valueOf(authentication.getName());
        String token = request.getHeader("Authorization");
        return ResponseEntity.ok(budgetService.listByUserWithRealTimeStatus(userId, token));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> update(@PathVariable Long id, @RequestBody BudgetDto dto, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(budgetService.update(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        budgetService.delete(id, userId);
        return ResponseEntity.ok().build();
    }
}