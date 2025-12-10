package eci.ieti.FinzenGoalService.service;

import eci.ieti.FinzenGoalService.dto.GoalDto;
import eci.ieti.FinzenGoalService.mapper.GoalMapper;
import eci.ieti.FinzenGoalService.mapper.GoalTransactionMapper;
import eci.ieti.FinzenGoalService.model.Goal;
import eci.ieti.FinzenGoalService.model.GoalCategory;
import eci.ieti.FinzenGoalService.model.GoalTransaction;
import eci.ieti.FinzenGoalService.repository.GoalRepository;
import eci.ieti.FinzenGoalService.repository.GoalTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock private GoalRepository goalRepository;
    @Mock private GoalTransactionRepository transactionRepository;
    @Mock private GoalMapper goalMapper;
    @Mock private GoalTransactionMapper transactionMapper;

    @InjectMocks private GoalService goalService;

    @Test
    void create_setsDefaults_andPersists() {
        GoalDto inputDto = new GoalDto(null, 10L, "Viaje", "Ir a la playa", new BigDecimal("1000"), null, LocalDate.now(), GoalCategory.TRAVEL, null);
        Goal entity = buildGoal();
        when(goalMapper.toEntity(inputDto)).thenReturn(entity);
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalMapper.toDto(any(Goal.class))).thenReturn(new GoalDto(1L, 10L, "Viaje", "Ir a la playa", new BigDecimal("1000"), BigDecimal.ZERO, entity.getDueDate(), GoalCategory.TRAVEL, "ACTIVE"));

        GoalDto result = goalService.create(inputDto);

        assertThat(result.getSavedAmount()).isEqualByComparingTo("0");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void deposit_updatesAmount_andMarksCompleted() {
        Goal goal = buildGoal();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalMapper.toDto(any(Goal.class))).thenReturn(new GoalDto(1L, goal.getUserId(), goal.getName(), goal.getDescription(), goal.getTargetAmount(), new BigDecimal("1200"), goal.getDueDate(), goal.getCategory(), "COMPLETED"));

        GoalDto result = goalService.deposit(1L, new BigDecimal("1200"));

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(transactionRepository).save(any(GoalTransaction.class));
    }

    @Test
    void deposit_rejectsNonPositive() {
        assertThatThrownBy(() -> goalService.deposit(1L, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deposit_throwsWhenGoalNotFound() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.deposit(99L, new BigDecimal("10")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void withdraw_rejectsInsufficientFunds() {
        Goal goal = buildGoal();
        goal.setSavedAmount(new BigDecimal("100"));
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> goalService.withdraw(1L, new BigDecimal("200")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withdraw_rejectsNegativeAmount() {
        assertThatThrownBy(() -> goalService.withdraw(1L, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withdraw_fromCompleted_reopensIfBelowTarget() {
        Goal goal = buildGoal();
        goal.setSavedAmount(new BigDecimal("1000"));
        goal.setStatus("COMPLETED");
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalMapper.toDto(any(Goal.class))).thenReturn(new GoalDto(1L, goal.getUserId(), goal.getName(), goal.getDescription(), goal.getTargetAmount(), new BigDecimal("400"), goal.getDueDate(), goal.getCategory(), "ACTIVE"));

        GoalDto result = goalService.withdraw(1L, new BigDecimal("600"));

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(transactionRepository).save(any(GoalTransaction.class));
    }

    @Test
    void update_throwsWhenNotFound() {
        when(goalRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.update(2L, new GoalDto(), 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void update_throwsWhenUnauthorized() {
        Goal goal = buildGoal();
        goal.setUserId(99L);
        when(goalRepository.findById(2L)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> goalService.update(2L, new GoalDto(), 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void delete_throwsWhenUnauthorized() {
        Goal goal = buildGoal();
        goal.setUserId(99L);
        when(goalRepository.findById(3L)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> goalService.delete(3L, 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(goalRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.delete(3L, 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getHistory_returnsMappedTransactions() {
        when(transactionRepository.findByGoalIdOrderByDateDesc(1L)).thenReturn(List.of());
        when(transactionMapper.toDtoList(List.of())).thenReturn(List.of());

        assertThat(goalService.getHistory(1L)).isEmpty();
    }

    @Test
    void listByUser_returnsEmptyList() {
        when(goalRepository.findByUserId(1L)).thenReturn(List.of());

        assertThat(goalService.listByUser(1L)).isEmpty();
    }

    @Test
    void listByUser_returnsMappedGoals() {
        Goal goal = buildGoal();
        GoalDto dto = new GoalDto(1L, 10L, "Viaje", "Ir a la playa", new BigDecimal("1000"), BigDecimal.ZERO, goal.getDueDate(), GoalCategory.TRAVEL, "ACTIVE");
        
        when(goalRepository.findByUserId(10L)).thenReturn(List.of(goal));
        when(goalMapper.toDto(goal)).thenReturn(dto);

        List<GoalDto> result = goalService.listByUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Viaje");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.getById(99L))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getById_returnsGoal() {
        Goal goal = buildGoal();
        GoalDto dto = new GoalDto(1L, 10L, "Viaje", "Ir a la playa", new BigDecimal("1000"), BigDecimal.ZERO, goal.getDueDate(), GoalCategory.TRAVEL, "ACTIVE");
        
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalMapper.toDto(goal)).thenReturn(dto);

        GoalDto result = goalService.getById(1L);

        assertThat(result.getName()).isEqualTo("Viaje");
    }

    @Test
    void update_savesChanges() {
        Goal goal = buildGoal();
        GoalDto updateDto = new GoalDto(1L, 10L, "Nuevo Nombre", "Nueva desc", new BigDecimal("2000"), BigDecimal.ZERO, goal.getDueDate(), GoalCategory.EDUCATION, "ACTIVE");
        
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalMapper.toDto(any(Goal.class))).thenReturn(updateDto);

        GoalDto result = goalService.update(1L, updateDto, 10L);

        assertThat(result.getName()).isEqualTo("Nuevo Nombre");
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void delete_removesGoalAndTransactions() {
        Goal goal = buildGoal();
        List<GoalTransaction> transactions = List.of(new GoalTransaction());
        
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepository.findByGoalIdOrderByDateDesc(1L)).thenReturn(transactions);

        goalService.delete(1L, 10L);

        verify(transactionRepository).deleteAll(transactions);
        verify(goalRepository).delete(goal);
    }

    @Test
    void deposit_addsAmount() {
        Goal goal = buildGoal();
        goal.setSavedAmount(new BigDecimal("100"));
        
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        GoalDto dto = new GoalDto(1L, 10L, "Viaje", "Ir a la playa", new BigDecimal("1000"), new BigDecimal("150"), goal.getDueDate(), GoalCategory.TRAVEL, "ACTIVE");
        when(goalMapper.toDto(any(Goal.class))).thenReturn(dto);

        GoalDto result = goalService.deposit(1L, new BigDecimal("50"));

        assertThat(result.getSavedAmount()).isEqualByComparingTo("150");
        verify(transactionRepository).save(any(GoalTransaction.class));
    }

    @Test
    void withdraw_subtractsAmount() {
        Goal goal = buildGoal();
        goal.setSavedAmount(new BigDecimal("500"));
        
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        GoalDto dto = new GoalDto(1L, 10L, "Viaje", "Ir a la playa", new BigDecimal("1000"), new BigDecimal("400"), goal.getDueDate(), GoalCategory.TRAVEL, "ACTIVE");
        when(goalMapper.toDto(any(Goal.class))).thenReturn(dto);

        GoalDto result = goalService.withdraw(1L, new BigDecimal("100"));

        assertThat(result.getSavedAmount()).isEqualByComparingTo("400");
        verify(transactionRepository).save(any(GoalTransaction.class));
    }

    @Test
    void withdraw_throwsWhenGoalNotFound() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.withdraw(99L, new BigDecimal("10")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void create_withNullSavedAmount_setsToZero() {
        GoalDto inputDto = new GoalDto(null, 10L, "Meta", "Desc", new BigDecimal("1000"), null, LocalDate.now(), GoalCategory.OTHER, null);
        Goal entity = buildGoal();
        entity.setSavedAmount(null);
        
        when(goalMapper.toEntity(inputDto)).thenReturn(entity);
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal saved = inv.getArgument(0);
            assertThat(saved.getSavedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            return saved;
        });
        when(goalMapper.toDto(any(Goal.class))).thenReturn(new GoalDto(1L, 10L, "Meta", "Desc", new BigDecimal("1000"), BigDecimal.ZERO, entity.getDueDate(), GoalCategory.OTHER, "ACTIVE"));

        GoalDto result = goalService.create(inputDto);

        assertThat(result.getSavedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Goal buildGoal() {
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setUserId(10L);
        goal.setName("Viaje");
        goal.setDescription("Ir a la playa");
        goal.setTargetAmount(new BigDecimal("1000"));
        goal.setSavedAmount(BigDecimal.ZERO);
        goal.setDueDate(LocalDate.now().plusDays(10));
        goal.setCategory(GoalCategory.TRAVEL);
        goal.setStatus("ACTIVE");
        return goal;
    }
}
