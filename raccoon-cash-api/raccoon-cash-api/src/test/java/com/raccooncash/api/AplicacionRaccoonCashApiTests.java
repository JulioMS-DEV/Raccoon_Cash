package com.raccooncash.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:raccoon_cash_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "raccoon-cash.data.seed-default-categories=false"
})
class AplicacionRaccoonCashApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void getTransactionsReturnsEmptyListWhenThereAreNoTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getTransactionsReturnsSameFlatContractAsCreateTransaction() throws Exception {
        Long accountId = createAccount();
        Long categoryId = createCategory();

        String transactionRequest = """
                {
                  "description": "Pago salario",
                  "amount": 12000,
                  "type": "INCOME",
                  "date": "2026-06-03T10:00:00",
                  "accountId": %d,
                  "categoryId": %d,
                  "notes": "Ingreso mensual"
                }
                """.formatted(accountId, categoryId);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account").doesNotExist())
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.accountName").value("Efectivo"))
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.categoryName").value("Salario"));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].account").doesNotExist())
                .andExpect(jsonPath("$[0].category").doesNotExist())
                .andExpect(jsonPath("$[0].accountId").value(accountId))
                .andExpect(jsonPath("$[0].accountName").value("Efectivo"))
                .andExpect(jsonPath("$[0].categoryId").value(categoryId))
                .andExpect(jsonPath("$[0].categoryName").value("Salario"));
    }

    @Test
    void createAndUpdateCategoryPersistParentCategoryId() throws Exception {
        Long parentId = createCategory("Comida", "EXPENSE");

        MvcResult childResult = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Samantha",
                                  "type": "EXPENSE",
                                  "color": "#7E57C2",
                                  "icon": "flower",
                                  "parentCategoryId": %d
                                }
                                """.formatted(parentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentCategoryId").value(parentId))
                .andReturn();

        Number childIdNumber = JsonPath.read(childResult.getResponse().getContentAsString(), "$.id");
        Long childId = childIdNumber.longValue();

        MvcResult categoriesResult = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andReturn();

        List<Number> parentIds = JsonPath.read(
                categoriesResult.getResponse().getContentAsString(),
                "$[?(@.id == " + childId + ")].parentCategoryId");
        assertThat(parentIds).hasSize(1);
        assertThat(parentIds.get(0).longValue()).isEqualTo(parentId);

        Long updatedParentId = createCategory("Servicios", "EXPENSE");

        mockMvc.perform(put("/api/categories/{id}", childId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Samantha",
                                  "type": "EXPENSE",
                                  "color": "#7E57C2",
                                  "icon": "flower",
                                  "parentCategoryId": %d
                                }
                                """.formatted(updatedParentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentCategoryId").value(updatedParentId));
    }

    @Test
    void deleteBudgetThroughSpanishEndpointRemovesItFromActiveList() throws Exception {
        MvcResult createdBudget = mockMvc.perform(post("/api/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Presupuesto diario",
                                  "monto": 10000,
                                  "tipoPeriodo": "DIARIO",
                                  "valorPeriodo": 1,
                                  "fechaInicio": "2026-06-22",
                                  "color": "#22C55E",
                                  "esGasto": true,
                                  "incluirTodasLasTransacciones": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Presupuesto diario"))
                .andExpect(jsonPath("$.tipoPeriodo").value("DIARIO"))
                .andExpect(jsonPath("$.fechaFin").value("2026-06-22"))
                .andExpect(jsonPath("$.montoActual").value(0))
                .andReturn();

        Number budgetId = JsonPath.read(createdBudget.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(put("/api/presupuestos/{id}", budgetId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Presupuesto diario actualizado",
                                  "monto": 15000,
                                  "tipoPeriodo": "DIARIO",
                                  "valorPeriodo": 2,
                                  "fechaInicio": "2026-06-22",
                                  "color": "#3B82F6",
                                  "esGasto": true,
                                  "incluirTodasLasTransacciones": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Presupuesto diario actualizado"))
                .andExpect(jsonPath("$.tipoPeriodo").value("DIARIO"))
                .andExpect(jsonPath("$.fechaFin").value("2026-06-23"));

        mockMvc.perform(delete("/api/presupuestos/{id}", budgetId.longValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/presupuestos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void savingBudgetCanTrackIncomeCategoryLimits() throws Exception {
        Long accountId = createAccount();
        Long categoryId = createCategory("Ahorro", "INCOME");

        MvcResult createdBudget = mockMvc.perform(post("/api/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Presupuesto de ahorro",
                                  "monto": 1000,
                                  "tipoPeriodo": "MENSUAL",
                                  "valorPeriodo": 1,
                                  "fechaInicio": "2026-06-01",
                                  "color": "#22C55E",
                                  "esGasto": false,
                                  "incluirTodasLasTransacciones": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esGasto").value(false))
                .andReturn();

        Number budgetId = JsonPath.read(createdBudget.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/presupuestos/{id}/categories", budgetId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "amountLimit": 1000
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(categoryId));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Aporte de ahorro",
                                  "amount": 300,
                                  "type": "INCOME",
                                  "date": "2026-06-15T10:00:00",
                                  "accountId": %d,
                                  "categoryId": %d
                                }
                                """.formatted(accountId, categoryId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/presupuestos/{id}", budgetId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoActual").value(300));
    }

    private Long createAccount() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Efectivo",
                                  "type": "CASH",
                                  "initialBalance": 5000,
                                  "currency": "C$",
                                  "color": "#4CAF50"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long createCategory() throws Exception {
        return createCategory("Salario", "INCOME");
    }

    private Long createCategory(String name, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "%s",
                                  "color": "#00BCD4",
                                  "icon": "work"
                                }
                                """.formatted(name, type)))
                .andExpect(status().isOk())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
