package eci.ieti.FinzenGoalService.controller;

import eci.ieti.FinzenGoalService.dto.GoalDto;
import eci.ieti.FinzenGoalService.dto.GoalTransactionDto;
import eci.ieti.FinzenGoalService.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalDto> create(@Valid @RequestBody GoalDto dto, Authentication authentication) {
        dto.setUserId(Long.valueOf(authentication.getName()));
        return ResponseEntity.ok(goalService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<GoalDto>> list(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(goalService.listByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto> update(@PathVariable Long id, @RequestBody GoalDto dto, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(goalService.update(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        goalService.delete(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<GoalDto> deposit(@PathVariable Long id, @RequestBody Map<String, BigDecimal> payload) {
        return ResponseEntity.ok(goalService.deposit(id, payload.get("amount")));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<GoalDto> withdraw(@PathVariable Long id, @RequestBody Map<String, BigDecimal> payload) {
        return ResponseEntity.ok(goalService.withdraw(id, payload.get("amount")));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<GoalTransactionDto>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getHistory(id));
    }
}