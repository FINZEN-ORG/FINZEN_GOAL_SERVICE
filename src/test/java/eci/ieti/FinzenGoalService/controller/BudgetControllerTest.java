package eci.ieti.FinzenGoalService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eci.ieti.FinzenGoalService.dto.BudgetDto;
import eci.ieti.FinzenGoalService.security.JwtAuthenticationFilter;
import eci.ieti.FinzenGoalService.security.JwtTokenProvider;
import eci.ieti.FinzenGoalService.security.SecurityConfig;
import eci.ieti.FinzenGoalService.service.BudgetService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BudgetController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JwtAuthenticationFilter.class,
                JwtTokenProvider.class,
                SecurityConfig.class
        })
)
@AutoConfigureMockMvc(addFilters = true)
class BudgetControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private BudgetService budgetService;

    @Test
    @WithMockUser(username = "1")
    void create_setsUserFromAuth() throws Exception {
        BudgetDto request = sampleDto();
        BudgetDto response = sampleDto();
        response.setId(5L);
        when(budgetService.createOrUpdate(any(BudgetDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/budgets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(username = "1")
    void list_forwardsAuthHeader() throws Exception {
        BudgetDto dto = sampleDto();
        when(budgetService.listByUserWithRealTimeStatus(eq(1L), eq("Bearer token"))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/budgets").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(2));
    }

    @Test
    @WithMockUser(username = "1")
    void update_passesUserId() throws Exception {
        BudgetDto body = sampleDto();
        body.setAmount(new BigDecimal("200"));
        when(budgetService.update(eq(10L), any(BudgetDto.class), eq(1L))).thenReturn(body);

        mockMvc.perform(put("/api/budgets/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    @WithMockUser(username = "1")
    void delete_invokesService() throws Exception {
        Mockito.doNothing().when(budgetService).delete(3L, 1L);

        mockMvc.perform(delete("/api/budgets/3").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void list_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isUnauthorized());
    }

    private BudgetDto sampleDto() {
        BudgetDto dto = new BudgetDto();
        dto.setUserId(1L);
        dto.setCategoryId(2L);
        dto.setAmount(new BigDecimal("100"));
        dto.setSpent(BigDecimal.ZERO);
        return dto;
    }
}
