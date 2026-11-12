package com.example.lostfound;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebMvcTest({ItemController.class, InfoController.class})
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    private void create(String json) throws Exception {
        mvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void listIsInitiallyEmpty() throws Exception {
        mvc.perform(get("/items")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createItemDefaultsToLost() throws Exception {
        mvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Umbrella\",\"location\":\"Library\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("LOST"))
                .andExpect(jsonPath("$.createdAt").exists());
        mvc.perform(get("/items")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void blankNameReturns400() throws Exception {
        mvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"location\":\"Library\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankLocationReturns400() throws Exception {
        mvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Keys\",\"location\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filterByStatus() throws Exception {
        create("{\"name\":\"Keys\",\"location\":\"Canteen\",\"status\":\"FOUND\"}");
        create("{\"name\":\"Wallet\",\"location\":\"Gym\",\"status\":\"LOST\"}");
        mvc.perform(get("/items").param("status", "FOUND"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Keys"));
        mvc.perform(get("/items")).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void claimSetsClaimed() throws Exception {
        create("{\"name\":\"Keys\",\"location\":\"Canteen\",\"status\":\"FOUND\"}");
        mvc.perform(post("/items/1/claim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLAIMED"));
    }

    @Test
    void claimTwiceStaysClaimed() throws Exception {
        create("{\"name\":\"Keys\",\"location\":\"Canteen\"}");
        mvc.perform(post("/items/1/claim")).andExpect(status().isOk());
        mvc.perform(post("/items/1/claim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLAIMED"));
    }

    @Test
    void deleteReturns204Then404() throws Exception {
        create("{\"name\":\"Keys\",\"location\":\"Canteen\"}");
        mvc.perform(delete("/items/1")).andExpect(status().isNoContent());
        mvc.perform(delete("/items/1")).andExpect(status().isNotFound());
    }

    @Test
    void claimUnknownReturns404() throws Exception {
        mvc.perform(post("/items/999/claim")).andExpect(status().isNotFound());
    }

    @Test
    void infoReturnsPodAndVersion() throws Exception {
        mvc.perform(get("/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pod").exists())
                .andExpect(jsonPath("$.version").value("1.0"));
    }
}
