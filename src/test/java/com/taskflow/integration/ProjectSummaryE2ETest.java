package com.taskflow.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verificación end-to-end contra la semilla H2: comprueba los resúmenes de proyectos según specs/summary.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class ProjectSummaryE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void summary_seed_h2_coincideConEspecificacion() throws Exception {
        // Login con usuario sembrado 'ana' (ver DataSeeder)
        String loginJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"ana\", \"password\": \"ana123\" }"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(loginJson, "$.token");
        String bearer = "Bearer " + token;

        // GET /projects/1/summary -> según spec: total 5, TODO 3, IN_PROGRESS 1, DONE 1, overdue 0
        mockMvc.perform(get("/projects/1/summary").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.totalTasks").value(5))
                .andExpect(jsonPath("$.byStatus.TODO").value(3))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(1))
                .andExpect(jsonPath("$.byStatus.DONE").value(1))
                .andExpect(jsonPath("$.overdue").value(0));

        // GET /projects/2/summary -> según spec: total 4, TODO 1, IN_PROGRESS 2, DONE 1, overdue 1
        mockMvc.perform(get("/projects/2/summary").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(2))
                .andExpect(jsonPath("$.totalTasks").value(4))
                .andExpect(jsonPath("$.byStatus.TODO").value(1))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(2))
                .andExpect(jsonPath("$.byStatus.DONE").value(1))
                .andExpect(jsonPath("$.overdue").value(1));

        // GET /projects/3/summary -> vacío: todos ceros
        mockMvc.perform(get("/projects/3/summary").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(3))
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.byStatus.TODO").value(0))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(0))
                .andExpect(jsonPath("$.byStatus.DONE").value(0))
                .andExpect(jsonPath("$.overdue").value(0));

        // GET /projects/99/summary -> 404
        mockMvc.perform(get("/projects/99/summary").header("Authorization", bearer))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
