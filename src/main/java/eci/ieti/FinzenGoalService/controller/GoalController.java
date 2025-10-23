package eci.ieti.FinzenGoalService.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eci.ieti.FinzenGoalService.dto.request.GoalRequestDto;
import eci.ieti.FinzenGoalService.dto.response.GoalResponseDto;
import eci.ieti.FinzenGoalService.service.impl.GoalServiceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    private GoalServiceimpl goalService;

    @PostMapping
    public ResponseEntity<GoalResponseDto> createGoal(@RequestBody GoalRequestDto entity) {
        if (entity == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(goalService.createGoal(entity));
    }

    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getAllGoals() {
        return ResponseEntity.ok(goalService.getAllGoals());
    }

    @GetMapping("/{name}")
    public ResponseEntity<GoalResponseDto> getGoalByName(String name) {
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(goalService.getGoalByName(name));
    }
    
    @PutMapping("/{name}")
    public ResponseEntity<GoalResponseDto> updateGoal(@RequestBody GoalRequestDto entity, @PathVariable String name) {
        if (entity == null || name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(goalService.updateGoal(entity, name));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteGoal(@PathVariable String name) {
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        goalService.deleteGoal(name);
        return ResponseEntity.noContent().build();
    }

    
}
