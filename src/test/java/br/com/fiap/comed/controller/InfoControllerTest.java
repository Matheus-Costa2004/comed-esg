package br.com.fiap.comed.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InfoController.class, properties = {
        "comed.versao=4f2a9c1e8b7d6a5f4e3d2c1b0a9f8e7d6c5b4a39",
        "comed.ambiente=staging" })
class InfoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void infoExpoeVersaoEAmbienteVindosDoAmbiente() throws Exception {
        mvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aplicacao").value("comed-esg"))
                .andExpect(jsonPath("$.versao").value("4f2a9c1e8b7d6a5f4e3d2c1b0a9f8e7d6c5b4a39"))
                .andExpect(jsonPath("$.ambiente").value("staging"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
