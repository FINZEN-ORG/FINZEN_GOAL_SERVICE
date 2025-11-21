package eci.ieti.FinzenGoalService.controller;

import eci.ieti.FinzenGoalService.dto.GoalDto;
import eci.ieti.FinzenGoalService.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
}