package eci.ieti.FinzenGoalService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eci.ieti.FinzenGoalService.dto.GoalDto;
import eci.ieti.FinzenGoalService.dto.GoalTransactionDto;
import eci.ieti.FinzenGoalService.model.GoalCategory;
import eci.ieti.FinzenGoalService.security.JwtAuthenticationFilter;
import eci.ieti.FinzenGoalService.security.JwtTokenProvider;
import eci.ieti.FinzenGoalService.security.SecurityConfig;
import eci.ieti.FinzenGoalService.service.GoalService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = GoalController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        SecurityConfig.class
    })
)
@AutoConfigureMockMvc(addFilters = true)
class GoalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private GoalService goalService;

    @Test
    @WithMockUser(username = "1")
    void create_setsUserFromAuth() throws Exception {
        GoalDto request = new GoalDto(null, null, "Viaje", "Ir a la playa", new BigDecimal("1000"), null, LocalDate.now(), GoalCategory.TRAVEL, null);
        GoalDto response = new GoalDto(5L, 1L, "Viaje", "Ir a la playa", new BigDecimal("1000"), BigDecimal.ZERO, request.getDueDate(), GoalCategory.TRAVEL, "ACTIVE");
        when(goalService.create(any(GoalDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "1")
    void list_returnsGoalsForUser() throws Exception {
        when(goalService.listByUser(1L)).thenReturn(List.of(new GoalDto(2L, 1L, "Meta", null, new BigDecimal("500"), BigDecimal.ZERO, null, GoalCategory.OTHER, "ACTIVE")));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @WithMockUser(username = "1")
    void update_passesUserId() throws Exception {
        GoalDto body = new GoalDto(null, null, "Actualizada", null, new BigDecimal("800"), null, null, GoalCategory.OTHER, null);
        GoalDto updated = new GoalDto(2L, 1L, "Actualizada", null, new BigDecimal("800"), BigDecimal.ZERO, null, GoalCategory.OTHER, "ACTIVE");
        when(goalService.update(eq(2L), any(GoalDto.class), eq(1L))).thenReturn(updated);

        mockMvc.perform(put("/api/goals/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Actualizada"));
    }

    @Test
    @WithMockUser(username = "1")
    void delete_invokesService() throws Exception {
        Mockito.doNothing().when(goalService).delete(3L, 1L);

        mockMvc.perform(delete("/api/goals/3").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1")
    void deposit_callsService() throws Exception {
        GoalDto response = new GoalDto(4L, 1L, "Meta", null, new BigDecimal("500"), new BigDecimal("50"), null, GoalCategory.OTHER, "ACTIVE");
        when(goalService.deposit(eq(4L), any(BigDecimal.class))).thenReturn(response);

        mockMvc.perform(post("/api/goals/4/deposit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("50")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savedAmount").value(50));
    }

    @Test
    @WithMockUser(username = "1")
    void withdraw_callsService() throws Exception {
        GoalDto response = new GoalDto(4L, 1L, "Meta", null, new BigDecimal("500"), new BigDecimal("10"), null, GoalCategory.OTHER, "ACTIVE");
        when(goalService.withdraw(eq(4L), any(BigDecimal.class))).thenReturn(response);

        mockMvc.perform(post("/api/goals/4/withdraw")
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("5")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savedAmount").value(10));
    }

    @Test
    @WithMockUser(username = "1")
    void history_returnsList() throws Exception {
        GoalTransactionDto tx = new GoalTransactionDto();
        tx.setType("DEPOSIT");
        when(goalService.getHistory(4L)).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/goals/4/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

    @Test
    void list_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized());
    }
}
