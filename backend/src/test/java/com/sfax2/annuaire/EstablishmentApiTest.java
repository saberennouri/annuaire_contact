package com.sfax2.annuaire;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.admin.password=test-pass", "spring.datasource.url=jdbc:h2:mem:apitest"})
@AutoConfigureMockMvc
class EstablishmentApiTest {

    private static final String NEW_ESTABLISHMENT = """
            {"category":"PRIMARY","delegation":"قرقنة","number":99,"name":"مدرسة اختبار",
             "phones":[],"contacts":[{"role":"DIRECTOR","fullName":"مدير اختبار","phones":["20123456"]}]}
            """;

    @Autowired
    MockMvc mvc;

    @Test
    void seedMatchesSourceDocuments() throws Exception {
        mvc.perform(get("/api/establishments/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SECONDARY").value(63))
                .andExpect(jsonPath("$.PRIMARY").value(192))
                .andExpect(jsonPath("$.PRIVATE_PRIMARY").value(14))
                .andExpect(jsonPath("$.PRESCHOOL").value(2))
                .andExpect(jsonPath("$.INSPECTION").value(25));
    }

    @Test
    void pagingAndFilters() throws Exception {
        mvc.perform(get("/api/establishments").param("category", "PRIMARY").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(192))
                .andExpect(jsonPath("$.hasNext").value(true));
        mvc.perform(get("/api/establishments").param("category", "SECONDARY").param("kind", "LYCEE"))
                .andExpect(jsonPath("$.totalElements").value(20));
        mvc.perform(get("/api/establishments").param("category", "PRIMARY").param("delegation", "قرقنة"))
                .andExpect(jsonPath("$.totalElements").value(9));
    }

    @Test
    void searchIgnoresArabicVariants() throws Exception {
        mvc.perform(get("/api/establishments").param("q", "الحنشه"))
                .andExpect(jsonPath("$.totalElements").value(greaterThan(0)));
    }

    @Test
    void searchByPhoneNumber() throws Exception {
        mvc.perform(get("/api/establishments").param("q", "97 362 117"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].category").value("SECONDARY"));
    }

    @Test
    void invalidPageSizeIsRejected() throws Exception {
        mvc.perform(get("/api/establishments").param("size", "5000")).andExpect(status().isBadRequest());
    }

    @Test
    void unknownIdReturns404() throws Exception {
        mvc.perform(get("/api/establishments/999999")).andExpect(status().isNotFound());
    }

    @Test
    void writesRequireAdmin() throws Exception {
        mvc.perform(post("/api/establishments").contentType(MediaType.APPLICATION_JSON).content(NEW_ESTABLISHMENT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateFindAndDelete() throws Exception {
        String location = mvc.perform(post("/api/establishments").with(httpBasic("admin", "test-pass"))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_ESTABLISHMENT))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get("/api/establishments").param("q", "20123456"))
                .andExpect(jsonPath("$.content[*].name").value(hasItem("مدرسة اختبار")));

        mvc.perform(delete(location).with(httpBasic("admin", "test-pass"))).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
    }
}
