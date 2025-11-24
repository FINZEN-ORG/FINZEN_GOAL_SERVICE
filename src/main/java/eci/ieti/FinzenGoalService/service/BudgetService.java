package eci.ieti.FinzenGoalService.service;

import eci.ieti.FinzenGoalService.dto.BudgetDto;
import eci.ieti.FinzenGoalService.dto.TransactionSummaryDto;
import eci.ieti.FinzenGoalService.mapper.BudgetMapper;
import eci.ieti.FinzenGoalService.model.Budget;
import eci.ieti.FinzenGoalService.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetMapper mapper;
    private final WebClient transactionWebClient;

    public BudgetDto createOrUpdate(BudgetDto dto) {
        Budget budget = mapper.toEntity(dto);
        Budget saved = budgetRepository.save(budget);
        BudgetDto response = mapper.toDto(saved);
        response.setSpent(BigDecimal.ZERO);
        return response;
    }

    /**
     * Recupera los presupuestos y consulta los gastos reales al microservicio de transacciones.
     * @param userId ID del usuario
     * @param token Token JWT "Bearer ..." para autenticarse ante TransactionService
     */
    public List<BudgetDto> listByUserWithRealTimeStatus(Long userId, String token) {
        // 1. Obtener la configuración de budgets de la DB
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        if (budgets.isEmpty()) {
            return List.of();
        }

        // 2. Calcular rango de fechas (Por defecto: Mes actual)
        // Se puede mejorar esto tomando las fechas de cada budget, pero para MVP asumimos mes actual
        LocalDateTime start = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        // 3. API COMPOSITION: Llamar a TransactionService
        List<TransactionSummaryDto> summaries;
        try {
            summaries = transactionWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/transactions/summary")
                            .queryParam("startDate", start)
                            .queryParam("endDate", end)
                            .build())
                    .header("Authorization", token) // Propagamos el token del usuario
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TransactionSummaryDto>>() {})
                    .block(); // Bloqueamos (Sync) porque necesitamos los datos para responder
        } catch (Exception e) {
            // Fallback: Si TransactionService falla, devolvemos los budgets con spent = 0
            // o lanzamos error dependiendo de la regla de negocio. Aquí Fail-Safe (spent 0).
            System.err.println("Error fetching transactions: " + e.getMessage());
            summaries = List.of();
        }

        // 4. Convertir lista de summaries a un Mapa para búsqueda rápida por CategoryId
        Map<Long, BigDecimal> expenseMap = summaries.stream()
                .collect(Collectors.toMap(
                        TransactionSummaryDto::getCategoryId,
                        TransactionSummaryDto::getTotalAmount
                ));

        // 5. Cruzar datos y armar DTOs finales
        return budgets.stream().map(budget -> {
            BudgetDto dto = mapper.toDto(budget);
            // Buscamos si hay gastos para la categoría de este budget
            BigDecimal spentAmount = expenseMap.getOrDefault(budget.getCategoryId(), BigDecimal.ZERO);
            dto.setSpent(spentAmount);
            return dto;
        }).collect(Collectors.toList());
    }
}