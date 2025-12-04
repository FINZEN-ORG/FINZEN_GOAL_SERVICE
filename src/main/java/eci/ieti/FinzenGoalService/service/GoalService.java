package eci.ieti.FinzenGoalService.service;

import eci.ieti.FinzenGoalService.dto.GoalDto;
import eci.ieti.FinzenGoalService.dto.GoalTransactionDto;
import eci.ieti.FinzenGoalService.mapper.GoalMapper;
import eci.ieti.FinzenGoalService.mapper.GoalTransactionMapper;
import eci.ieti.FinzenGoalService.model.Goal;
import eci.ieti.FinzenGoalService.model.GoalTransaction;
import eci.ieti.FinzenGoalService.repository.GoalRepository;
import eci.ieti.FinzenGoalService.repository.GoalTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final GoalTransactionRepository transactionRepository;
    private final GoalMapper mapper;
    private final GoalTransactionMapper transactionMapper;

    public GoalDto create(GoalDto dto) {
        Goal entity = mapper.toEntity(dto);
        if (entity.getSavedAmount() == null) entity.setSavedAmount(BigDecimal.ZERO);
        entity.setStatus(entity.getStatus() == null ? "ACTIVE" : entity.getStatus());
        Goal saved = goalRepository.save(entity);
        return mapper.toDto(saved);
    }

    public List<GoalDto> listByUser(Long userId) {
        return goalRepository.findByUserId(userId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public GoalDto getById(Long id) {
        return goalRepository.findById(id).map(mapper::toDto).orElseThrow();
    }

    // --- ACTUALIZAR (PUT) ---
    @Transactional
    public GoalDto update(Long id, GoalDto dto, Long userId) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        if (!goal.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setDueDate(dto.getDueDate());
        goal.setCategory(dto.getCategory());
        // No actualizamos savedAmount aquí, solo vía deposit/withdraw
        return mapper.toDto(goalRepository.save(goal));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        if (!goal.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
        // Opcional: Borrar transacciones primero si no tienes Cascade en DB
        transactionRepository.deleteAll(transactionRepository.findByGoalIdOrderByDateDesc(id));
        goalRepository.delete(goal);
    }

    // --- TRANSACCIONES CON HISTORIAL ---
    @Transactional
    public GoalDto deposit(Long goalId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new RuntimeException("Goal not found"));
        goal.setSavedAmount(goal.getSavedAmount().add(amount));
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("COMPLETED");
        }
        goalRepository.save(goal);
        // Guardar Historial
        GoalTransaction tx = new GoalTransaction(goalId, amount, "DEPOSIT", "Abono a meta");
        transactionRepository.save(tx);
        return mapper.toDto(goal);
    }

    @Transactional
    public GoalDto withdraw(Long goalId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new RuntimeException("Goal not found"));
        if (goal.getSavedAmount().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds");
        goal.setSavedAmount(goal.getSavedAmount().subtract(amount));
        if ("COMPLETED".equals(goal.getStatus()) && goal.getSavedAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus("ACTIVE");
        }
        goalRepository.save(goal);
        // Guardar Historial
        GoalTransaction tx = new GoalTransaction(goalId, amount, "WITHDRAW", "Retiro de meta");
        transactionRepository.save(tx);
        return mapper.toDto(goal);
    }

    public List<GoalTransactionDto> getHistory(Long goalId) {
        return transactionMapper.toDtoList(transactionRepository.findByGoalIdOrderByDateDesc(goalId));
    }
}