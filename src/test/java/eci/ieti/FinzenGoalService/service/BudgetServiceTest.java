package eci.ieti.FinzenGoalService.service;

import eci.ieti.FinzenGoalService.dto.BudgetDto;
import eci.ieti.FinzenGoalService.dto.TransactionSummaryDto;
import eci.ieti.FinzenGoalService.mapper.BudgetMapper;
import eci.ieti.FinzenGoalService.model.Budget;
import eci.ieti.FinzenGoalService.repository.BudgetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private BudgetMapper mapper;
    @Mock private WebClient transactionWebClient;

    @InjectMocks private BudgetService budgetService;

    @Test
    void createOrUpdate_throwsIfDuplicateCategoryForUser() {
        BudgetDto dto = sampleDto();
        when(budgetRepository.findByUserIdAndCategoryId(dto.getUserId(), dto.getCategoryId()))
                .thenReturn(Optional.of(new Budget()));

        assertThatThrownBy(() -> budgetService.createOrUpdate(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createOrUpdate_savesAndSetsSpentZero() {
        BudgetDto dto = sampleDto();
        Budget budget = sampleBudget();
        when(budgetRepository.findByUserIdAndCategoryId(dto.getUserId(), dto.getCategoryId()))
                .thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(budget);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        BudgetDto savedDto = sampleDto();
        savedDto.setSpent(BigDecimal.ZERO);
        when(mapper.toDto(any(Budget.class))).thenReturn(savedDto);

        BudgetDto result = budgetService.createOrUpdate(dto);

        assertThat(result.getSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void listByUser_returnsEmptyWhenNoBudgets() {
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of());

        assertThat(budgetService.listByUserWithRealTimeStatus(1L, "Bearer x"))
                .isEmpty();
    }

    @Test
    void listByUser_mapsExpensesFromTransactionService() {
        Budget budget = sampleBudget();
        budget.setCategoryId(5L);
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(mapper.toDto(budget)).thenReturn(dtoFromBudget(budget));
        mockWebClientCall(List.of(new TransactionSummaryDto(5L, new BigDecimal("123.45"))));

        List<BudgetDto> result = budgetService.listByUserWithRealTimeStatus(1L, "Bearer token");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpent()).isEqualByComparingTo("123.45");
    }

    @Test
    void listByUser_fallbackWhenTransactionServiceFails() {
        Budget budget = sampleBudget();
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(mapper.toDto(budget)).thenReturn(dtoFromBudget(budget));
        mockWebClientCall(null); // Signal to throw

        List<BudgetDto> result = budgetService.listByUserWithRealTimeStatus(1L, "Bearer token");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.update(1L, new BudgetDto(), 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void update_throwsWhenUnauthorized() {
        Budget budget = sampleBudget();
        budget.setUserId(99L);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.update(1L, new BudgetDto(), 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void delete_throwsWhenUnauthorized() {
        Budget budget = sampleBudget();
        budget.setUserId(99L);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.delete(1L, 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void update_savesSuccessfully() {
        Budget budget = sampleBudget();
        BudgetDto updateDto = sampleDto();
        updateDto.setAmount(new BigDecimal("200"));
        
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any(Budget.class))).thenReturn(updateDto);

        BudgetDto result = budgetService.update(1L, updateDto, 1L);

        assertThat(result.getAmount()).isEqualByComparingTo("200");
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void delete_removesSuccessfully() {
        Budget budget = sampleBudget();
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        budgetService.delete(1L, 1L);

        verify(budgetRepository).delete(budget);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.delete(1L, 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void createOrUpdate_updatesExistingBudget() {
        BudgetDto dto = sampleDto();
        dto.setId(10L);
        Budget budget = sampleBudget();
        
        when(mapper.toEntity(dto)).thenReturn(budget);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        BudgetDto savedDto = sampleDto();
        savedDto.setSpent(BigDecimal.ZERO);
        when(mapper.toDto(any(Budget.class))).thenReturn(savedDto);

        BudgetDto result = budgetService.createOrUpdate(dto);

        assertThat(result.getSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(budgetRepository).save(any(Budget.class));
        verify(budgetRepository, never()).findByUserIdAndCategoryId(anyLong(), anyLong());
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientCall(List<TransactionSummaryDto> response) {
        ParameterizedTypeReference<List<TransactionSummaryDto>> typeRef = new ParameterizedTypeReference<>() {};
        @SuppressWarnings("rawtypes")
        RequestHeadersUriSpec uriSpec = (RequestHeadersUriSpec) org.mockito.Mockito.mock(RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RequestHeadersSpec headersSpec = (RequestHeadersSpec) org.mockito.Mockito.mock(RequestHeadersSpec.class);
        ResponseSpec responseSpec = org.mockito.Mockito.mock(ResponseSpec.class);

        when(transactionWebClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        
        if (response == null) {
            when(responseSpec.bodyToMono(typeRef)).thenThrow(new RuntimeException("boom"));
        } else {
            when(responseSpec.bodyToMono(typeRef)).thenReturn(Mono.just(response));
        }
    }

    private BudgetDto sampleDto() {
        BudgetDto dto = new BudgetDto();
        dto.setUserId(1L);
        dto.setCategoryId(2L);
        dto.setAmount(new BigDecimal("100"));
        return dto;
    }

    private Budget sampleBudget() {
        Budget budget = new Budget();
        budget.setId(10L);
        budget.setUserId(1L);
        budget.setCategoryId(2L);
        budget.setAmount(new BigDecimal("100"));
        return budget;
    }

    private BudgetDto dtoFromBudget(Budget budget) {
        BudgetDto dto = new BudgetDto();
        dto.setId(budget.getId());
        dto.setUserId(budget.getUserId());
        dto.setCategoryId(budget.getCategoryId());
        dto.setAmount(budget.getAmount());
        dto.setSpent(BigDecimal.ZERO);
        return dto;
    }
}
