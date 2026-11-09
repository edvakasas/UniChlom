package lt.samata.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The demo scenario end to end: HTTP request -> price list from the database -> core module -> saved estimate. */
@SpringBootTest
@AutoConfigureMockMvc
class EstimateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void estimateOverLimit_isSaved_thenApproved() throws Exception {
        String request = """
                {"carClass": "VISUREIGIS", "regularCustomer": true, "workCodes": ["D01", "D02", "D05"],
                 "parts": [{"name": "Stabdžių kaladėlės", "unitCost": 30.00, "quantity": 1},
                           {"name": "Stabdžių diskai", "unitCost": 45.00, "quantity": 2}],
                 "priceLimit": 250.00}
                """;

        String body = mockMvc.perform(post("/api/estimates").header("X-Tenant-Id", "alfa")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estimate.total").value(326.22))
                .andExpect(jsonPath("$.estimate.status").value("LAUKIA_PATVIRTINIMO"))
                .andExpect(jsonPath("$.estimate.overLimitBy").value(76.22))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(body, "$.id");

        mockMvc.perform(post("/api/estimates/{id}/approve", id).header("X-Tenant-Id", "alfa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimate.status").value("PATVIRTINTA"));
        mockMvc.perform(post("/api/estimates/{id}/approve", id).header("X-Tenant-Id", "alfa"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/estimates/{id}", id).header("X-Tenant-Id", "alfa"))
                .andExpect(jsonPath("$.estimate.total").value(326.22));
    }
}
