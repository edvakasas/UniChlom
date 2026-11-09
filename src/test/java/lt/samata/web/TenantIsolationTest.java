package lt.samata.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Workshop "alfa" must not be able to read or change workshop "beta"'s data, whatever id it sends. */
@SpringBootTest
@AutoConfigureMockMvc
class TenantIsolationTest {

    private static final String HEADER = "X-Tenant-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void alfaCannotReadBetaEstimate() throws Exception {
        int betaEstimateId = createBetaEstimateAwaitingApproval();

        mockMvc.perform(get("/api/estimates/{id}", betaEstimateId).header(HEADER, "alfa"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]").value("Sąmata nerasta: " + betaEstimateId));
        mockMvc.perform(get("/api/estimates").header(HEADER, "alfa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(betaEstimateId))));
    }

    @Test
    void alfaCannotApproveBetaEstimate() throws Exception {
        int betaEstimateId = createBetaEstimateAwaitingApproval();

        mockMvc.perform(post("/api/estimates/{id}/approve", betaEstimateId).header(HEADER, "alfa"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/estimates/{id}", betaEstimateId).header(HEADER, "beta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimate.status").value("LAUKIA_PATVIRTINIMO"));
    }

    @Test
    void alfaCannotUseBetaPriceList() throws Exception {
        String request = """
                {"carClass": "LENGVASIS", "regularCustomer": false, "workCodes": ["B03"], "parts": []}
                """;

        mockMvc.perform(post("/api/estimates").header(HEADER, "alfa")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Nežinomas darbo kodas: B03"));
    }

    private int createBetaEstimateAwaitingApproval() throws Exception {
        String request = """
                {"carClass": "LENGVASIS", "regularCustomer": false, "workCodes": ["B04", "B05"],
                 "parts": [], "priceLimit": 10.00}
                """;
        String body = mockMvc.perform(post("/api/estimates").header(HEADER, "beta")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estimate.status").value("LAUKIA_PATVIRTINIMO"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }
}
